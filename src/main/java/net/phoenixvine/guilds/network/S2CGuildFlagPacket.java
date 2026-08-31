package net.phoenixvine.guilds.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.phoenixvine.guilds.PhoenixGuilds;
import net.phoenixvine.guilds.data.Guild;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record S2CGuildFlagPacket(
        UUID guildId,
        String flagIconId,
        String flagPixelData,
        boolean flagUseDrawing,
        int flagWidth,
        int flagHeight
) implements CustomPacketPayload {

    public static final Type<S2CGuildFlagPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PhoenixGuilds.MOD_ID, "guild_flag"));

    public static final StreamCodec<ByteBuf, S2CGuildFlagPacket> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            S2CGuildFlagPacket::guildId,
            ByteBufCodecs.stringUtf8(GuildNetworkLimits.ICON_ID_MAX),
            S2CGuildFlagPacket::flagIconId,
            ByteBufCodecs.stringUtf8(Guild.FLAG_PIXEL_DATA_LENGTH),
            S2CGuildFlagPacket::flagPixelData,
            ByteBufCodecs.BOOL,
            S2CGuildFlagPacket::flagUseDrawing,
            ByteBufCodecs.VAR_INT,
            S2CGuildFlagPacket::flagWidth,
            ByteBufCodecs.VAR_INT,
            S2CGuildFlagPacket::flagHeight,
            S2CGuildFlagPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final S2CGuildFlagPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            
            GuildPacketHandlerClient.handleClientFlagPacket(packet);
        });
    }
}