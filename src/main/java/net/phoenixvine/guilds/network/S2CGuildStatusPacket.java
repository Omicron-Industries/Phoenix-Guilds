package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;

public record S2CGuildStatusPacket(String message, boolean error) {

    public S2CGuildStatusPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(GuildNetworkLimits.LOG_MESSAGE_MAX), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(message, GuildNetworkLimits.LOG_MESSAGE_MAX);
        buf.writeBoolean(error);
    }
}
