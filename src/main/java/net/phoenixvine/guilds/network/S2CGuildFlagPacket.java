package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.phoenixvine.guilds.data.Guild;

import java.util.UUID;

public record S2CGuildFlagPacket(
                                 UUID guildId,
                                 String flagIconId,
                                 String flagPixelData,
                                 boolean flagUseDrawing,
                                 int flagWidth,
                                 int flagHeight) {

    public S2CGuildFlagPacket(FriendlyByteBuf buf) {
        this(
                buf.readUUID(),
                buf.readUtf(GuildNetworkLimits.ICON_ID_MAX),
                buf.readUtf(Guild.FLAG_PIXEL_DATA_LENGTH),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(guildId);
        buf.writeUtf(flagIconId, GuildNetworkLimits.ICON_ID_MAX);
        buf.writeUtf(flagPixelData, Guild.FLAG_PIXEL_DATA_LENGTH);
        buf.writeBoolean(flagUseDrawing);
        buf.writeVarInt(flagWidth);
        buf.writeVarInt(flagHeight);
    }
}
