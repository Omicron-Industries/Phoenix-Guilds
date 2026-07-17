package net.phoenixvine.guilds.content.flag;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Stores only which guild this placed flag belongs to — deliberately not its own icon/width
 * /height. Right-clicking any flag block for a guild updates that GUILD's flag ({@code Guild
 * .flagIconId}/width/height, via {@code GuildManager.setFlag}), which every placed flag block for
 * that guild then reads live at render time — so all of a guild's flags update together, matching
 * "clicking the flag changes the texture" being a guild-level action, not a per-block override.
 */
public class GuildFlagBlockEntity extends BlockEntity {

    private UUID guildId;

    public GuildFlagBlockEntity(BlockPos pos, BlockState state) {
        super(GuildFlagBlocks.GUILD_FLAG_ENTITY.get(), pos, state);
    }

    public UUID getGuildId() {
        return guildId;
    }

    /**
     * {@code setChanged()} alone only marks this entity dirty for the next disk save — it does
     * NOT push a network update to any client already watching this chunk (verified against
     * decompiled {@code BlockEntity.java}: the default {@code getUpdateTag()}/{@code
     * getUpdatePacket()} this class didn't override return an empty tag / {@code null},
     * so nothing was ever sent). Without the explicit {@code sendBlockUpdated} call below, a
     * player standing right next to the flag when it's placed (or right-clicked to a new guild
     * owner) would never see the guildId this sets — their client's copy of this entity stays
     * whatever it was at chunk-load time until they leave and re-enter render distance, which is
     * exactly what "the flag never shows anything in-world" turned out to be. Mirrors {@code
     * BannerBlockEntity}'s own {@code getUpdateTag}/{@code getUpdatePacket} shape exactly.
     */
    public void setGuildId(UUID guildId) {
        this.guildId = guildId;
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (guildId != null) tag.putUUID("guildId", guildId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        guildId = tag.hasUUID("guildId") ? tag.getUUID("guildId") : null;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    /**
     * The renderer draws the flag near the top of the UPPER half's block space (local Y up to
     * ~1.875 relative to this — the LOWER half's — own origin, per {@code
     * GuildFlagBlockEntityRenderer#RENDER_Y}), well past this block's own 1x1x1 AABB — expanded
     * upward for that. The -Z expansion is now extra headroom rather than a strict requirement
     * (the dynamic content centers on {@code RENDER_Z = 0}, not draped into the southern
     * neighbor the way an earlier model's "flagface" element was) — kept anyway since it's cheap
     * insurance against the exact same "flag vanishes when this entity's own cell falls out of
     * the view frustum but a neighboring one is still visible" class of bug.
     */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).expandTowards(0, 1, -1);
    }
}
