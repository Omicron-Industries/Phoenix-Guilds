package net.phoenixvine.guilds.content.flag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.phoenixvine.guilds.PhoenixGuilds;
import net.phoenixvine.guilds.client.ClientGuildCache;
import net.phoenixvine.guilds.client.GuildFlagContentRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public class GuildFlagItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final ResourceLocation POLE_TEXTURE = new ResourceLocation(PhoenixGuilds.MOD_ID,
            "textures/block/guilds_flag.png");
    private static final float POLE_MIN = 7f / 16f;
    private static final float POLE_MAX = 9f / 16f;
    private static final float POLE_TOP = 2f;
    private static final float CAP_V = 2f / 32f;

    public GuildFlagItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet entityModelSet) {
        super(dispatcher, entityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (displayContext == ItemDisplayContext.GUI) {
            renderGuiIcon(poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        poseStack.pushPose();
        renderPole(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        if (!ClientGuildCache.isInGuild()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        poseStack.pushPose();

        GuildFlagContentRenderer.render(mc.player.getUUID(), ClientGuildCache.flagUseDrawing,
                ClientGuildCache.flagIconId, ClientGuildCache.flagPixelData, ClientGuildCache.flagWidth,
                ClientGuildCache.flagHeight, net.minecraft.core.Direction.NORTH, poseStack, buffer, packedLight,
                packedOverlay, mc.level);
        poseStack.popPose();
    }

    private void renderGuiIcon(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        String iconId = ClientGuildCache.isInGuild() ? ClientGuildCache.flagIconId : "";
        String pixelData = ClientGuildCache.isInGuild() ? ClientGuildCache.flagPixelData : "";
        boolean useDrawing = ClientGuildCache.isInGuild() && ClientGuildCache.flagUseDrawing;

        GuildFlagContentRenderer.renderIcon(mc.player.getUUID(), useDrawing, iconId, pixelData,
                ClientGuildCache.flagWidth, ClientGuildCache.flagHeight, poseStack, buffer, packedLight,
                packedOverlay, mc.level);
    }

    private static void renderPole(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                   int packedOverlay) {
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vc = buffer.getBuffer(RenderType.entitySolid(POLE_TEXTURE));
        float x0 = POLE_MIN, x1 = POLE_MAX, z0 = POLE_MIN, z1 = POLE_MAX, y0 = 0f, y1 = POLE_TOP;

        vertex(vc, matrix, x0, y1, z1, 0, 0, 0, 0, 1, packedLight, packedOverlay);
        vertex(vc, matrix, x0, y0, z1, 0, 1, 0, 0, 1, packedLight, packedOverlay);
        vertex(vc, matrix, x1, y0, z1, 1, 1, 0, 0, 1, packedLight, packedOverlay);
        vertex(vc, matrix, x1, y1, z1, 1, 0, 0, 0, 1, packedLight, packedOverlay);

        vertex(vc, matrix, x1, y1, z0, 0, 0, 0, 0, -1, packedLight, packedOverlay);
        vertex(vc, matrix, x1, y0, z0, 0, 1, 0, 0, -1, packedLight, packedOverlay);
        vertex(vc, matrix, x0, y0, z0, 1, 1, 0, 0, -1, packedLight, packedOverlay);
        vertex(vc, matrix, x0, y1, z0, 1, 0, 0, 0, -1, packedLight, packedOverlay);

        vertex(vc, matrix, x1, y1, z1, 0, 0, 1, 0, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x1, y0, z1, 0, 1, 1, 0, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x1, y0, z0, 1, 1, 1, 0, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x1, y1, z0, 1, 0, 1, 0, 0, packedLight, packedOverlay);

        vertex(vc, matrix, x0, y1, z0, 0, 0, -1, 0, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x0, y0, z0, 0, 1, -1, 0, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x0, y0, z1, 1, 1, -1, 0, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x0, y1, z1, 1, 0, -1, 0, 0, packedLight, packedOverlay);

        vertex(vc, matrix, x0, y1, z0, 0, 0, 0, 1, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x0, y1, z1, 0, CAP_V, 0, 1, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x1, y1, z1, 1, CAP_V, 0, 1, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x1, y1, z0, 1, 0, 0, 1, 0, packedLight, packedOverlay);

        vertex(vc, matrix, x0, y0, z0, 0, 0, 0, -1, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x1, y0, z0, 1, 0, 0, -1, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x1, y0, z1, 1, CAP_V, 0, -1, 0, packedLight, packedOverlay);
        vertex(vc, matrix, x0, y0, z1, 0, CAP_V, 0, -1, 0, packedLight, packedOverlay);
    }

    private static void vertex(VertexConsumer vc, Matrix4f matrix, float x, float y, float z, float u, float v,
                               float nx, float ny, float nz, int packedLight, int packedOverlay) {
        vc.vertex(matrix, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(packedOverlay).uv2(packedLight)
                .normal(nx, ny, nz).endVertex();
    }
}
