package net.phoenixvine.guilds.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.phoenixvine.guilds.PhoenixGuilds;

import java.util.Optional;

public class GuildNetwork {

    private static final String PROTOCOL = "1";
    // Change from public static final to public static
    public static SimpleChannel CHANNEL;

    private static int id = 0;

    public static void init() {
        // Instantiate the channel safely here
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(PhoenixGuilds.MOD_ID, "main"),
                () -> PROTOCOL,
                PROTOCOL::equals,
                PROTOCOL::equals);

        // ── Phoenix Guilds ────────────────────────────────────────────────────
        CHANNEL.registerMessage(id++,
                C2SGuildActionPacket.class,
                C2SGuildActionPacket::encode,
                C2SGuildActionPacket::new,
                C2SGuildActionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++,
                S2CGuildSyncPacket.class,
                S2CGuildSyncPacket::encode,
                S2CGuildSyncPacket::new,
                S2CGuildSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(id++,
                S2COpenGuildScreenPacket.class,
                S2COpenGuildScreenPacket::encode,
                S2COpenGuildScreenPacket::new,
                S2COpenGuildScreenPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
