package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;
import net.phoenixvine.guilds.event.GuildActions;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public record C2SSetGuildFlagPacket(
                                    @Nullable UUID targetGuildId,
                                    boolean useDrawing,
                                    String iconId,
                                    String pixelData,
                                    int width,
                                    int height) {

    public static C2SSetGuildFlagPacket decode(FriendlyByteBuf buf) {
        UUID targetGuildId = buf.readBoolean() ? buf.readUUID() : null;
        boolean useDrawing = buf.readBoolean();
        String iconId = buf.readUtf(GuildNetworkLimits.ICON_ID_MAX);
        String pixelData = buf.readUtf(Guild.FLAG_PIXEL_DATA_LENGTH);
        int width = buf.readVarInt();
        int height = buf.readVarInt();
        return new C2SSetGuildFlagPacket(targetGuildId, useDrawing, iconId, pixelData, width, height);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(targetGuildId != null);
        if (targetGuildId != null) buf.writeUUID(targetGuildId);
        buf.writeBoolean(useDrawing);
        buf.writeUtf(iconId, GuildNetworkLimits.ICON_ID_MAX);
        buf.writeUtf(pixelData, Guild.FLAG_PIXEL_DATA_LENGTH);
        buf.writeVarInt(width);
        buf.writeVarInt(height);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            GuildManager mgr = GuildManager.get(Objects.requireNonNull(player.getServer()).overworld());
            GuildActions.handleSetFlag(player, mgr, targetGuildId, useDrawing, iconId, pixelData, width, height);
        });
        ctx.get().setPacketHandled(true);
    }
}
