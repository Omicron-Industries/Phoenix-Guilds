package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.guilds.PhoenixGuilds;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public record C2SRequestGuildFlagPacket(UUID guildId) implements CustomPacketPayload {

    public static final Type<C2SRequestGuildFlagPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PhoenixGuilds.MOD_ID, "request_guild_flag"));

    public static final StreamCodec<FriendlyByteBuf, C2SRequestGuildFlagPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeUUID(packet.guildId),
            buf -> new C2SRequestGuildFlagPacket(buf.readUUID())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final C2SRequestGuildFlagPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            GuildManager mgr = GuildManager.get(player.getServer().overworld());
            Optional<Guild> opt = mgr.getGuildById(payload.guildId());

            S2CGuildFlagPacket reply = opt.map(g -> new S2CGuildFlagPacket(
                    payload.guildId(),
                    g.getFlagIconId(),
                    g.getFlagPixelData(),
                    g.isFlagUseDrawing(),
                    g.getFlagWidth(),
                    g.getFlagHeight()
            )).orElseGet(() -> new S2CGuildFlagPacket(
                    payload.guildId(),
                    "",
                    "",
                    false,
                    Guild.DEFAULT_FLAG_SIZE,
                    Guild.DEFAULT_FLAG_SIZE
            ));

            PacketDistributor.sendToPlayer(player, reply);
        });
    }
}