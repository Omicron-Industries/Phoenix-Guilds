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

/**
 * The hand-painted flag mode — a free-RGB pixel grid (every pixel can be any color, not limited
 * to a fixed palette). Kept as an alternative to (not a replacement for) {@link
 * GuildFlagIconManager}'s icon mode; {@code Guild.flagUseDrawing} picks which one is
 * authoritative.
 * <p>
 * Each pixel is stored as 6 lowercase hex chars (RRGGBB, always opaque) in {@code Guild
 * .flagPixelData} rather than an index into a fixed set of swatches — {@link #PALETTE} still
 * exists, but only as a set of quick-pick starting points in the editor UI, not as the storage
 * format.
 * <p>
 * The grid's drawable resolution tracks the guild's configured {@code flagWidth}/{@code
 * flagHeight} directly (one cell per configured pixel — {@link #cols}/{@link #rows}), so raising
 * the resolution slider genuinely exposes more drawable tiles rather than just stretching the
 * same fixed grid larger. {@code Guild.flagPixelData} is always stored at a fixed {@link
 * #MAX_DIM}x{@link #MAX_DIM} stride regardless of the currently-active width/height, so
 * shrinking/growing the resolution never scrambles or discards already-painted pixels outside
 * the currently-visible window.
 */
public final class GuildFlagPixelArt {

    public static final int MAX_DIM = Guild.MAX_FLAG_SIZE;
    /** Chars stored per pixel in {@code Guild.flagPixelData} — 6 lowercase hex digits, RRGGBB. */
    public static final int CHARS_PER_PIXEL = 6;

    /** Quick-pick starting swatches for the editor UI only — not a storage-format constraint. */
    public static final int[] PALETTE = {
            0xFFFFFF, 0x9D9D97, 0x474F52, 0x1D1D21,
            0x835432, 0xB02E26, 0xF9801D, 0xFED83D,
            0x80C71F, 0x5E7C16, 0x169C9C, 0x3AB3DA,
            0x3C44AA, 0x8932B8, 0xC74EBD, 0xF38BAA,
    };

    private GuildFlagPixelArt() {}

    /** Reads the RRGGBB color at pixel {@code idx} (row*MAX_DIM+col) out of the stored string. */
    public static int colorAt(String pixelData, int idx) {
        int start = idx * CHARS_PER_PIXEL;
        try {
            return Integer.parseInt(pixelData, start, start + CHARS_PER_PIXEL, 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    /** Formats an RRGGBB color back into the 6-char lowercase hex form storage expects. */
    public static String toHex6(int rgb) {
        return String.format("%06x", rgb & 0xFFFFFF);
    }

    public static int cols(int flagWidth) {
        return Mth.clamp(flagWidth, 1, MAX_DIM);
    }

    public static int rows(int flagHeight) {
        return Mth.clamp(flagHeight, 1, MAX_DIM);
    }

    /**
     * Draws the active {@code cols(flagWidth)}x{@code rows(flagHeight)} window as flat cells at ({@code x}, {@code y}),
     * {@code width}x{@code height} pixels overall.
     */
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

    // ── 3D (BlockEntityRenderer) ────────────────────────────────────────────────

    /**
     * One baked texture per guild — a single shared cache slot would flicker/collide between two different guilds'
     * drawn flags rendering in the same frame.
     */
    private record Baked(String pixelData, int cols, int rows, ResourceLocation location) {}

    private static final Map<UUID, Baked> BAKED = new HashMap<>();

    /**
     * Bakes {@code pixelData} into a small runtime {@link DynamicTexture} (regenerated only when
     * that guild's data or resolution actually changes — same "bake once, reuse the
     * ResourceLocation" technique {@code WaypointIconManager}'s custom-icon loading and Solaris's
     * {@code SmoothShapes} both already use), then draws it as a single flat textured quad in
     * world-space.
     * <p>
     * Originally used {@code RenderType.text(location)} on the (wrong) assumption it was
     * cull-free, matching how map content in an item frame reads from both sides — verified
     * against decompiled {@code RenderType.java} that this isn't actually true: {@code
     * CompositeStateBuilder}'s default {@code cullState} is {@code CULL}, and vanilla's own
     * {@code TEXT} definition never overrides it, so the quad was only ever visible from
     * whichever single side its winding order happened to face (reported as the flag content
     * "pointed into the flag instead of being seeable" once the flag block's own two-block
     * geometry made the wrong-side case the common one). {@link RenderType#entityTranslucent}
     * explicitly sets {@code NO_CULL} — a single draw call is now genuinely visible from both
     * sides, no need to render twice at opposing angles the way the item-icon path still does.
     * Its vertex format ({@code DefaultVertexFormat.NEW_ENTITY}) adds an overlay coordinate and a
     * face normal versus the old format — {@link OverlayTexture#NO_OVERLAY} for the former (this
     * quad never needs the hurt/white-flash overlay tint), and the quad's own local-space normal
     * (0, 0, 1) for the latter, matching the vertex winding below (unaffected by this change).
     */
    public static void render3D(UUID guildId, String pixelData, int flagWidth, int flagHeight, PoseStack poseStack,
                                MultiBufferSource buffer, int packedLight) {
        if (pixelData == null || pixelData.length() < MAX_DIM * MAX_DIM * CHARS_PER_PIXEL) return;
        ResourceLocation texture = textureFor(guildId, pixelData, flagWidth, flagHeight);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        // V=0 is the texture's top row (NativeImage row 0, the first row baked in textureFor's
        // row-major setPixelRGBA loop) — the world-space top vertex (+Y) must sample V=0, not
        // V=1, or the drawn image comes out upside-down (rows read bottom-to-top).
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
                // NativeImage wants ABGR — same channel-swap convention every texture bake in
                // this mod family uses.
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
