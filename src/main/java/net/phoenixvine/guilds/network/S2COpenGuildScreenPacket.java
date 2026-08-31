package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.phoenixvine.guilds.PhoenixGuilds;
import net.phoenixvine.guilds.client.ClientPacketHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record S2COpenGuildScreenPacket() implements CustomPacketPayload {

    public static final Type<S2COpenGuildScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PhoenixGuilds.MOD_ID, "open_guild_screen"));

    public static final StreamCodec<FriendlyByteBuf, S2COpenGuildScreenPacket> STREAM_CODEC =
            StreamCodec.unit(new S2COpenGuildScreenPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final S2COpenGuildScreenPacket payload, final IPayloadContext context) {
        context.enqueueWork(ClientPacketHandler::openGuildScreen);
    }
}