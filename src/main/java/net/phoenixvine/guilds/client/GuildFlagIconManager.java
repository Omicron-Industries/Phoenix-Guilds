package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import static net.phoenixvine.guilds.client.GuildThemeUtils.C_FAINT;

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

    public static String iconIdFor(Item item) {
        if (item instanceof BlockItem blockItem && blockItem.getBlock() != Blocks.AIR) {
            return blockIconId(blockItem.getBlock());
        }
        return itemIconId(item);
    }

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
            
        }
        return ItemStack.EMPTY;
    }

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

    private static final float ITEM_DEPTH_SCALE = 0.05f;

    public static void render3D(String iconId, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                int packedOverlay, Level level) {
        ItemStack stack = resolveIcon(iconId);
        if (stack.isEmpty()) return;

        if (iconId != null && iconId.startsWith(BLOCK_PREFIX)) {
            renderFlatSprite(stack, poseStack, buffer, packedLight, level);
            return;
        }

        poseStack.pushPose();

        poseStack.scale(-1f, 1f, ITEM_DEPTH_SCALE);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, packedLight,
                packedOverlay, poseStack, buffer, level, 0);
        poseStack.popPose();
    }

    private static void renderFlatSprite(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                                         int packedLight, Level level) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, 0);
        TextureAtlasSprite sprite = model.getParticleIcon();

        int tint = Minecraft.getInstance().getItemColors().getColor(stack, 0);
        int r = tint == -1 ? 255 : tint >> 16 & 255;
        int g = tint == -1 ? 255 : tint >> 8 & 255;
        int b = tint == -1 ? 255 : tint & 255;

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();

        vertexConsumer.vertex(matrix, -0.5f, 0.5f, 0).color(r, g, b, 255).uv(u0, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, 0.5f, 0.5f, 0).color(r, g, b, 255).uv(u1, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, 0.5f, -0.5f, 0).color(r, g, b, 255).uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, -0.5f, -0.5f, 0).color(r, g, b, 255).uv(u0, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
    }
}
