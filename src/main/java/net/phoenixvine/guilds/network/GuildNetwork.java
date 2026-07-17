package net.phoenixvine.guilds.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.phoenixvine.guilds.PhoenixGuilds;

import java.util.Optional;

public class GuildNetwork {

    private static final String PROTOCOL = "2";
    public static SimpleChannel CHANNEL;
    private static int id = 0;

    public static void init() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(PhoenixGuilds.MOD_ID, "main"),
                () -> PROTOCOL,
                PROTOCOL::equals,
                PROTOCOL::equals);

        // ── SERVER-BOUND PACKETS ──
        CHANNEL.registerMessage(id++,
                C2SGuildActionPacket.class,
                C2SGuildActionPacket::encode,
                C2SGuildActionPacket::new,
                C2SGuildActionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++,
                C2SSetGuildFlagPacket.class,
                C2SSetGuildFlagPacket::encode,
                C2SSetGuildFlagPacket::new,
                C2SSetGuildFlagPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++,
                C2SRequestGuildFlagPacket.class,
                C2SRequestGuildFlagPacket::encode,
                C2SRequestGuildFlagPacket::new,
                C2SRequestGuildFlagPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        // ── CLIENT-BOUND PACKETS ──

        // 1. Guild Sync Packet
        CHANNEL.registerMessage(id++,
                S2CGuildSyncPacket.class,
                S2CGuildSyncPacket::encode,
                S2CGuildSyncPacket::new,
                (pkt, ctxSupplier) -> {
                    NetworkEvent.Context ctx = ctxSupplier.get();
                    ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                            () -> () -> GuildPacketHandlerClient.handleSyncPacket(pkt)));
                    ctx.setPacketHandled(true);
                },
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        // 2. Open Guild Screen Packet
        CHANNEL.registerMessage(id++,
                S2COpenGuildScreenPacket.class,
                S2COpenGuildScreenPacket::encode,
                S2COpenGuildScreenPacket::new,
                (pkt, ctxSupplier) -> {
                    NetworkEvent.Context ctx = ctxSupplier.get();
                    ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                            () -> () -> GuildPacketHandlerClient.handleOpenScreenPacket()));
                    ctx.setPacketHandled(true);
                },
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        // 3. Guild Flag Packet
        CHANNEL.registerMessage(id++,
                S2CGuildFlagPacket.class,
                S2CGuildFlagPacket::encode,
                S2CGuildFlagPacket::new,
                (pkt, ctxSupplier) -> {
                    NetworkEvent.Context ctx = ctxSupplier.get();
                    ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                            () -> () -> GuildPacketHandlerClient.handleClientFlagPacket(pkt)));
                    ctx.setPacketHandled(true);
                },
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
