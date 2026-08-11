package net.phoenixvine.guilds.content.flag;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

import javax.annotation.Nullable;

public class GuildFlagBlockEntity extends BlockEntity {

    private UUID guildId;

    private Direction facing = Direction.NORTH;

    public GuildFlagBlockEntity(BlockPos pos, BlockState state) {
        super(GuildFlagBlocks.GUILD_FLAG_ENTITY.get(), pos, state);
    }

    public UUID getGuildId() {
        return guildId;
    }

    public Direction getFacing() {
        return facing;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
        setChanged();
    }

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
        tag.putString("facing", facing.getSerializedName());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        guildId = tag.hasUUID("guildId") ? tag.getUUID("guildId") : null;
        facing = tag.contains("facing") ? Direction.byName(tag.getString("facing")) : Direction.NORTH;
        if (facing == null) facing = Direction.NORTH;
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

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos()).expandTowards(0, 1, -1);
    }
}
