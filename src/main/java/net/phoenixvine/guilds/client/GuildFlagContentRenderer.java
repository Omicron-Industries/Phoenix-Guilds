package net.phoenixvine.guilds.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.phoenixvine.guilds.PhoenixGuilds;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix4f;

import java.util.UUID;

import javax.annotation.Nullable;

public final class GuildFlagContentRenderer {

    private static final float MAX_SCALE_X = 1.0f;
    private static final float MAX_SCALE_Y = 0.5f;
    private static final double RENDER_Y = 1.625;
    private static final double RENDER_Z = 0.0;
    private static final float[] FACING_ROTATIONS_DEG = { 90f, -90f };
    private static final double Z_FIGHT_OFFSET = 0.005;
    private static final double BACKING_OFFSET = Z_FIGHT_OFFSET / 2;

    private static final ResourceLocation BACKING_TEXTURE = new ResourceLocation(PhoenixGuilds.MOD_ID,
            "textures/block/guild_flag_backing.png");

    private static final String SELF_RECURSIVE_ICON = "item:" + PhoenixGuilds.MOD_ID + ":guild_flag";

    private GuildFlagContentRenderer() {}

    private static float angleForFacing(Direction facing) {
        return switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 90f;
            case EAST -> -90f;
            default -> 0f; 
        };
    }

    private static void drawBacking(float scaleX, float scaleY, PoseStack poseStack, MultiBufferSource buffer,
                                    int packedLight) {
        for (float rotationDeg : FACING_ROTATIONS_DEG) {
            poseStack.pushPose();
            poseStack.translate(0.5 + Math.signum(rotationDeg) * BACKING_OFFSET, RENDER_Y, RENDER_Z);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
            poseStack.scale(scaleX, scaleY, 1f);
            Matrix4f matrix = poseStack.last().pose();
            VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(BACKING_TEXTURE));
            vc.vertex(matrix, -0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(0, 0)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            vc.vertex(matrix, 0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(1, 0)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            vc.vertex(matrix, 0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(1, 1)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            vc.vertex(matrix, -0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(0, 1)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            poseStack.popPose();
        }
    }

    public static void renderIcon(UUID guildId, boolean useDrawing, String iconId, String pixelData, int width,
                                  int height, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                  int packedOverlay, @Nullable Level level) {
        float scaleX = Math.min(MAX_SCALE_X, MAX_SCALE_X * (width / 16f));
        float scaleY = Math.min(MAX_SCALE_Y, MAX_SCALE_Y * (height / 16f));
        float iconScale = 0.7f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(scaleX * iconScale, scaleY * iconScale, 1f);
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(BACKING_TEXTURE));
        vc.vertex(matrix, -0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, 0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, 0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vc.vertex(matrix, -0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        poseStack.popPose();

        if (!useDrawing && iconId != null && iconId.equals(SELF_RECURSIVE_ICON)) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5 + BACKING_OFFSET);
        poseStack.scale(scaleX * iconScale, scaleY * iconScale, 1f);
        if (useDrawing) {
            GuildFlagPixelArt.render3D(guildId, pixelData, width, height, poseStack, buffer, packedLight);
        } else {
            GuildFlagIconManager.render3D(iconId, poseStack, buffer, packedLight, packedOverlay, level);
        }
        poseStack.popPose();
    }

    public static void render(UUID guildId, boolean useDrawing, String iconId, String pixelData, int width,
                              int height, Direction facing, PoseStack poseStack, MultiBufferSource buffer,
                              int packedLight, int packedOverlay, @Nullable Level level) {
        float scaleX = Math.min(MAX_SCALE_X, MAX_SCALE_X * (width / 16f));
        float scaleY = Math.min(MAX_SCALE_Y, MAX_SCALE_Y * (height / 16f));

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(angleForFacing(facing)));
        poseStack.translate(-0.5, 0, -0.5);

        drawBacking(scaleX, scaleY, poseStack, buffer, packedLight);

        if (useDrawing) {
            for (float rotationDeg : FACING_ROTATIONS_DEG) {
                poseStack.pushPose();
                poseStack.translate(0.5 + Math.signum(rotationDeg) * Z_FIGHT_OFFSET, RENDER_Y, RENDER_Z);
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                poseStack.scale(scaleX, scaleY, 1f);
                GuildFlagPixelArt.render3D(guildId, pixelData, width, height, poseStack, buffer, packedLight);
                poseStack.popPose();
            }
            poseStack.popPose();
            return;
        }

        if (iconId != null && iconId.equals(SELF_RECURSIVE_ICON)) {
            poseStack.popPose();
            return;
        }

        for (float rotationDeg : FACING_ROTATIONS_DEG) {
            poseStack.pushPose();
            poseStack.translate(0.5 + Math.signum(rotationDeg) * Z_FIGHT_OFFSET, RENDER_Y, RENDER_Z);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
            poseStack.scale(scaleX, scaleY, 1f);
            GuildFlagIconManager.render3D(iconId, poseStack, buffer, packedLight, packedOverlay, level);
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
