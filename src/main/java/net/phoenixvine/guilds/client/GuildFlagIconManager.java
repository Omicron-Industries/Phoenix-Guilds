package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import static net.phoenixvine.guilds.client.GuildThemeUtils.C_FAINT;

/**
 * Resolves and draws a guild's flag icon — an arbitrary item or block's own icon, referenced by
 * a {@code "item:<registry id>"}/{@code "block:<registry id>"}-prefixed string on {@code Guild
 * .flagIconId}. Mirrors Solaris's {@code WaypointIconManager} convention (not its code — Guilds
 * has no dependency on Solaris): same prefix scheme, same {@code ItemStack.EMPTY}/fallback
 * handling for an id that no longer resolves (item removed, mod uninstalled, typo'd save data).
 */
public final class GuildFlagIconManager {

    public static final String ITEM_PREFIX = "item:";
    public static final String BLOCK_PREFIX = "block:";

    private GuildFlagIconManager() {}

    public static String itemIconId(Item item) {
        return ITEM_PREFIX + ForgeRegistries.ITEMS.getKey(item);
    }

    public static String blockIconId(Block block) {
        return BLOCK_PREFIX + ForgeRegistries.BLOCKS.getKey(block);
    }

    /** Empty if {@code iconId} is blank, malformed, or no longer resolves to a real item/block. */
    public static ItemStack resolveIcon(String iconId) {
        if (iconId == null || iconId.isBlank()) return ItemStack.EMPTY;
        try {
            if (iconId.startsWith(ITEM_PREFIX)) {
                ResourceLocation id = new ResourceLocation(iconId.substring(ITEM_PREFIX.length()));
                Item item = ForgeRegistries.ITEMS.getValue(id);
                return item == null ? ItemStack.EMPTY : new ItemStack(item);
            }
            if (iconId.startsWith(BLOCK_PREFIX)) {
                ResourceLocation id = new ResourceLocation(iconId.substring(BLOCK_PREFIX.length()));
                Block block = ForgeRegistries.BLOCKS.getValue(id);
                return block == null || block == net.minecraft.world.level.block.Blocks.AIR ? ItemStack.EMPTY :
                        new ItemStack(block);
            }
        } catch (Exception ignored) {
            // Malformed id (bad ResourceLocation) — falls through to EMPTY below.
        }
        return ItemStack.EMPTY;
    }

    /**
     * Draws the resolved icon scaled to {@code width}x{@code height} pixels at ({@code x},
     * {@code y}) — same pose-stack-scale technique Solaris's {@code WaypointIcon#drawItem} uses
     * for its own item-icon rendering. Draws a faint placeholder square if {@code iconId} doesn't
     * resolve to anything, so an unset/broken flag still reads as "a flag" rather than a blank
     * gap.
     */
    public static void renderFlag(GuiGraphics g, String iconId, int x, int y, int width, int height) {
        ItemStack stack = resolveIcon(iconId);
        if (stack.isEmpty()) {
            g.fill(x, y, x + width, y + height, C_FAINT);
            return;
        }
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(width / 16f, height / 16f, 1f);
        g.renderItem(stack, 0, 0);
        g.pose().popPose();
    }

    /**
     * Draws the resolved icon as a single flat textured quad in world-space — a real picture of
     * the item/block, not the item's actual 3D model. {@code ItemRenderer.renderStatic} (the
     * previous approach here) always renders the item's full baked model: for a block icon that
     * means every visible face (top/side/front) still gets drawn and independently lit, so no
     * amount of squashing it thinner along one axis stops it from reading as a 3D object rather
     * than a picture of one (reported as "still tries to render the block as 3d"). Sampling the
     * model's own {@link BakedModel#getParticleIcon()} sprite and drawing it as a quad — the same
     * "grab a texture, draw one flat rectangle" technique {@code GuildFlagPixelArt.render3D}
     * already uses for hand-painted flags — sidesteps that category of problem entirely: there's
     * only ever one face to draw, so it can't look like anything but a flat picture. This also
     * means blocks and items need no separate depth-scale tuning, and no {@code
     * ItemDisplayContext.GUI}-specific winding/culling workaround (see the old code's own removed
     * comment on that) — none of that baked-model machinery is invoked at all anymore.
     * <p>
     * {@code getParticleIcon()} isn't always literally the item's "front" texture (for some
     * blocks it's a representative face chosen by the model, e.g. a log's side rather than its
     * end) but it's a single real, already-loaded texture with no extra baking step, and reads
     * correctly for the overwhelming majority of items/blocks — the same tradeoff vanilla itself
     * accepts when using this same accessor for break-particles.
     */
    public static void render3D(String iconId, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                Level level) {
        ItemStack stack = resolveIcon(iconId);
        if (stack.isEmpty()) return;

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, 0);
        TextureAtlasSprite sprite = model.getParticleIcon();

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        // Same V-orientation convention as GuildFlagPixelArt.render3D: the world-space top vertex
        // (+Y) samples the sprite's top row (v0), or the picture comes out upside-down.
        vertexConsumer.vertex(matrix, -0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(u0, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, 0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(u1, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, 0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, -0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(u0, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
    }
}
