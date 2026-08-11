package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.phoenixvine.guilds.network.C2SRequestGuildFlagPacket;
import net.phoenixvine.guilds.network.GuildNetwork;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientGuildFlagCache {

    private static final long REFRESH_INTERVAL_TICKS = 200; 

    public record FlagData(String iconId, String pixelData, boolean useDrawing, int width, int height) {}

    private static final Map<UUID, FlagData> CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_REQUESTED_TICK = new ConcurrentHashMap<>();

    private ClientGuildFlagCache() {}

    public static void put(UUID guildId, String iconId, String pixelData, boolean useDrawing, int width,
                           int height) {
        CACHE.put(guildId, new FlagData(iconId, pixelData, useDrawing, width, height));
    }

    public static FlagData getOrRequest(UUID guildId) {
        long now = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;
        Long lastRequested = LAST_REQUESTED_TICK.get(guildId);
        if (lastRequested == null || now - lastRequested >= REFRESH_INTERVAL_TICKS) {
            LAST_REQUESTED_TICK.put(guildId, now);
            GuildNetwork.CHANNEL.sendToServer(new C2SRequestGuildFlagPacket(guildId));
        }
        return CACHE.get(guildId);
    }

    public static void clear() {
        CACHE.clear();
        LAST_REQUESTED_TICK.clear();
    }
}
