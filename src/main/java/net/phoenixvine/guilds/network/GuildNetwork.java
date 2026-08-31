package net.phoenixvine.guilds.network;

import net.neoforged.bus.api.SubscribeEvent; 
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class GuildNetwork {

    @SubscribeEvent 
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                C2SGuildActionPacket.TYPE,
                C2SGuildActionPacket.STREAM_CODEC,
                C2SGuildActionPacket::handle
        );

        registrar.playToServer(
                C2SSetGuildFlagPacket.TYPE,
                C2SSetGuildFlagPacket.STREAM_CODEC,
                C2SSetGuildFlagPacket::handle
        );

        registrar.playToServer(
                C2SRequestGuildFlagPacket.TYPE,
                C2SRequestGuildFlagPacket.STREAM_CODEC,
                C2SRequestGuildFlagPacket::handle
        );

        registrar.playToClient(
                S2CGuildSyncPacket.TYPE,
                S2CGuildSyncPacket.STREAM_CODEC,
                S2CGuildSyncPacket::handle
        );

        registrar.playToClient(
                S2COpenGuildScreenPacket.TYPE,
                S2COpenGuildScreenPacket.STREAM_CODEC,
                S2COpenGuildScreenPacket::handle
        );

        registrar.playToClient(
                S2CGuildFlagPacket.TYPE,
                S2CGuildFlagPacket.STREAM_CODEC,
                S2CGuildFlagPacket::handle
        );
    }
}