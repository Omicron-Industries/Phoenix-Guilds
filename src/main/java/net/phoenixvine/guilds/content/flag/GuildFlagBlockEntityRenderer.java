package net.phoenixvine.guilds.content.flag;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.phoenixvine.guilds.client.ClientGuildFlagCache;
import net.phoenixvine.guilds.client.GuildFlagIconManager;
import net.phoenixvine.guilds.client.GuildFlagPixelArt;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import java.util.UUID;

/**
 * The first custom {@code BlockEntityRenderer} in this mod family — draws the owning guild's
 * current flag (an item/block icon, or a hand-painted pixel grid — see {@code Guild
 * .flagUseDrawing}) in 3D, hovering over the real block model's plain post (see
 * {@code guild_flag.json}). Reads the flag from {@link ClientGuildFlagCache}, which fetches it
 * lazily via a targeted request/response packet pair the first time (and periodically thereafter,
 * bounding staleness) rather than from a broadcast-to-everyone list — see {@code
 * S2CGuildSyncPacket.GuildSummary}'s own doc for why that broadcast no longer carries flag data at
 * all. Nothing is stored on the block entity itself beyond the owning {@code guildId}, so every
 * placed flag for a guild reflects that guild's current flag without any extra per-block sync.
 * <p>
 * {@link #RENDER_Y}/{@link #RENDER_Z}/{@link #MAX_SCALE_X}/{@link #MAX_SCALE_Y} are simply chosen
 * constants (not derived from any model geometry — an earlier version of the model had a
 * "flagface" placeholder element there whose own coordinates these were once copied from; that
 * element is gone now, see {@code GuildFlagBlock#getRenderShape}'s doc, but the same numbers
 * still place the flag at a sensible spot near the post's top): {@link #RENDER_Y} = 1.625 sits it
 * just below the post's own top cap, {@link #MAX_SCALE_X}/{@link #MAX_SCALE_Y} cap the flag at
 * roughly one block wide by half a block tall so it can't be scaled bigger than reads sensibly
 * next to a thin post. {@code flagWidth}/{@code flagHeight} can go up to {@code Guild
 * .MAX_FLAG_SIZE} (64) — a naive proportional scale from the 16-default would render 4x past
 * that cap at max resolution, so {@link Math#min} clamps it: resolutions below the 16-default
 * still render smaller, but nothing ever renders larger than the cap.
 * <p>
 * Both the pixel-art quad ({@code GuildFlagPixelArt.render3D}) and the item/block icon ({@code
 * GuildFlagIconManager.render3D}) draw a single flat textured quad with a genuinely {@code
 * NO_CULL} render type — no baked 3D model involved for either anymore (see {@code
 * GuildFlagIconManager.render3D}'s own doc for why the icon side moved off {@code
 * ItemRenderer.renderStatic}), so there's no per-mode depth-scale tuning or winding/culling
 * workaround needed. Both still render <b>twice</b> though, for an unrelated reason: the z-fight
 * offset below. The base model's own pole sits exactly at world X=0.5; our overlay is nudged to
 * one side of that (X=0.5±{@link #Z_FIGHT_OFFSET}) so the depth buffer doesn't have to arbitrate
 * between two coplanar surfaces. A single nudge in only one direction fixes z-fighting on that
 * side but then sits the overlay <i>behind</i> the model's own geometry as seen from the opposite
 * side — the model geometry, being closer to a camera on that side, wins the depth test and hides
 * the overlay entirely (reported as "the draw flag doesn't show on the back face"). Rendering both
 * a +offset and a -offset copy — each individually still just one flat plane, {@code NO_CULL}
 * making each visible from either direction — means whichever side the camera is actually on, the
 * copy nudged toward that side wins the depth test and shows; the copy nudged to the far side
 * loses the depth test against the model's own geometry and is invisibly cut away, exactly the
 * behavior wanted on both sides at once.
 */
public class GuildFlagBlockEntityRenderer implements BlockEntityRenderer<GuildFlagBlockEntity> {

    private static final float MAX_SCALE_X = 1.0f;
    private static final float MAX_SCALE_Y = 0.5f;
    private static final double RENDER_Y = 1.625;
    private static final double RENDER_Z = 0.0;
    private static final float[] FACING_ROTATIONS_DEG = { 90f, -90f };
    // Small enough to be visually imperceptible as a separate surface, large enough to survive
    // typical depth-buffer precision loss at a normal viewing distance — same order of magnitude
    // as other mods' own decal-offset constants for this exact problem.
    private static final double Z_FIGHT_OFFSET = 0.005;

    public GuildFlagBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(GuildFlagBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        UUID guildId = blockEntity.getGuildId();
        if (guildId == null) return;

        // Throttled internally (see ClientGuildFlagCache's own doc) — safe to call every frame
        // this block entity renders. Returns null on the very first render before any reply has
        // come back yet, in which case we just skip drawing this frame (same as every other
        // "nothing to draw yet" early-out here) rather than block on the network round trip.
        ClientGuildFlagCache.FlagData flag = ClientGuildFlagCache.getOrRequest(guildId);
        if (flag == null) return;

        float scaleX = Math.min(MAX_SCALE_X, MAX_SCALE_X * (flag.width() / 16f));
        float scaleY = Math.min(MAX_SCALE_Y, MAX_SCALE_Y * (flag.height() / 16f));

        if (flag.useDrawing()) {
            for (float rotationDeg : FACING_ROTATIONS_DEG) {
                poseStack.pushPose();
                poseStack.translate(0.5 + Math.signum(rotationDeg) * Z_FIGHT_OFFSET, RENDER_Y, RENDER_Z);
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                poseStack.scale(scaleX, scaleY, 1f);
                GuildFlagPixelArt.render3D(guildId, flag.pixelData(), flag.width(), flag.height(), poseStack, buffer,
                        packedLight);
                poseStack.popPose();
            }
            return;
        }

        for (float rotationDeg : FACING_ROTATIONS_DEG) {
            poseStack.pushPose();
            poseStack.translate(0.5 + Math.signum(rotationDeg) * Z_FIGHT_OFFSET, RENDER_Y, RENDER_Z);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
            poseStack.scale(scaleX, scaleY, 1f);
            GuildFlagIconManager.render3D(flag.iconId(), poseStack, buffer, packedLight, blockEntity.getLevel());
            poseStack.popPose();
        }
    }
}
