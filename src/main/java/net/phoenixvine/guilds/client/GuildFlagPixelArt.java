package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.phoenixvine.guilds.PhoenixGuilds;
import net.phoenixvine.guilds.data.Guild;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GuildFlagPixelArt {

    public static final int MAX_DIM = Guild.MAX_FLAG_SIZE;

    public static final int CHARS_PER_PIXEL = 6;

    public static final int[] PALETTE = {
            0xFFFFFF, 0x9D9D97, 0x474F52, 0x1D1D21,
            0x835432, 0xB02E26, 0xF9801D, 0xFED83D,
            0x80C71F, 0x5E7C16, 0x169C9C, 0x3AB3DA,
            0x3C44AA, 0x8932B8, 0xC74EBD, 0xF38BAA,
    };

    private GuildFlagPixelArt() {}

    public static int colorAt(String pixelData, int idx) {
        int start = idx * CHARS_PER_PIXEL;
        try {
            return Integer.parseInt(pixelData, start, start + CHARS_PER_PIXEL, 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    public static String toHex6(int rgb) {
        return String.format("%06x", rgb & 0xFFFFFF);
    }

    public static int cols(int flagWidth) {
        return Mth.clamp(flagWidth, 1, MAX_DIM);
    }

    public static int rows(int flagHeight) {
        return Mth.clamp(flagHeight, 1, MAX_DIM);
    }

    public static void render2D(GuiGraphics g, String pixelData, int flagWidth, int flagHeight, int x, int y,
                                int width, int height) {
        if (pixelData == null || pixelData.length() < MAX_DIM * MAX_DIM * CHARS_PER_PIXEL) return;
        int cols = cols(flagWidth);
        int rows = rows(flagHeight);
        float cellW = width / (float) cols;
        float cellH = height / (float) rows;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int color = 0xFF000000 | colorAt(pixelData, row * MAX_DIM + col);
                int cx = x + Math.round(col * cellW);
                int cy = y + Math.round(row * cellH);
                g.fill(cx, cy, x + Math.round((col + 1) * cellW), y + Math.round((row + 1) * cellH), color);
            }
        }
    }

    private record Baked(String pixelData, int cols, int rows, ResourceLocation location) {}

    private static final Map<UUID, Baked> BAKED = new HashMap<>();

    public static void render3D(UUID guildId, String pixelData, int flagWidth, int flagHeight, PoseStack poseStack,
                                MultiBufferSource buffer, int packedLight) {
        if (pixelData == null || pixelData.length() < MAX_DIM * MAX_DIM * CHARS_PER_PIXEL) return;
        ResourceLocation texture = textureFor(guildId, pixelData, flagWidth, flagHeight);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));

        vertexConsumer.vertex(matrix, -0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, 0.5f, 0.5f, 0).color(255, 255, 255, 255).uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, 0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, -0.5f, -0.5f, 0).color(255, 255, 255, 255).uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
    }

    private static ResourceLocation textureFor(UUID guildId, String pixelData, int flagWidth, int flagHeight) {
        int cols = cols(flagWidth);
        int rows = rows(flagHeight);
        Baked existing = BAKED.get(guildId);
        if (existing != null && existing.pixelData().equals(pixelData) && existing.cols() == cols &&
                existing.rows() == rows) {
            return existing.location();
        }

        NativeImage image = new NativeImage(cols, rows, false);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int rgb = colorAt(pixelData, row * MAX_DIM + col);

                int r = rgb >> 16 & 255;
                int gr = rgb >> 8 & 255;
                int b = rgb & 255;
                image.setPixelRGBA(col, row, 0xFF000000 | b << 16 | gr << 8 | r);
            }
        }
        DynamicTexture texture = new DynamicTexture(image);
        texture.setFilter(false, false);
        ResourceLocation loc = new ResourceLocation(PhoenixGuilds.MOD_ID,
                "dynamic/guild_flag_pixel_art_" + guildId);
        Minecraft.getInstance().getTextureManager().register(loc, texture);

        BAKED.put(guildId, new Baked(pixelData, cols, rows, loc));
        return loc;
    }
}
