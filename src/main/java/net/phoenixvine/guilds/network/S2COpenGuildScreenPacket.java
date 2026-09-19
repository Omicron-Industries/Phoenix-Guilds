package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;

public record S2COpenGuildScreenPacket() {

    public S2COpenGuildScreenPacket(FriendlyByteBuf buf) {
        this();
    }

    public void encode(FriendlyByteBuf buf) {}
}
