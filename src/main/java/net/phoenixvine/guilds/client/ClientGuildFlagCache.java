package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.phoenixvine.guilds.network.C2SRequestGuildFlagPacket;
import net.phoenixvine.guilds.network.GuildNetwork;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-guild flag data, fetched lazily via {@link C2SRequestGuildFlagPacket}/{@code
 * S2CGuildFlagPacket} instead of arriving bundled in the broadcast-to-everyone guild list (see
 * {@code S2CGuildSyncPacket.GuildSummary}'s own doc for why). {@link GuildFlagBlockEntityRenderer}
 * is the sole reader — it calls {@link #getOrRequest(UUID)} every time it's about to render a
 * placed flag, which both returns whatever's cached (possibly stale, possibly absent) and
 * triggers a background refresh when the entry is missing or old enough to be due for one.
 */
public final class ClientGuildFlagCache {

    /**
     * How long a cached flag is trusted before a background re-request is fired for it again —
     * bounds staleness (another player's flag edit reaches a bystander's placed block within
     * this window) without re-requesting every single render frame.
     */
    private static final long REFRESH_INTERVAL_TICKS = 200; // ~10s at 20 tps

    public record FlagData(String iconId, String pixelData, boolean useDrawing, int width, int height) {}

    private static final Map<UUID, FlagData> CACHE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_REQUESTED_TICK = new ConcurrentHashMap<>();

    private ClientGuildFlagCache() {}

    public static void put(UUID guildId, String iconId, String pixelData, boolean useDrawing, int width,
                           int height) {
        CACHE.put(guildId, new FlagData(iconId, pixelData, useDrawing, width, height));
    }

    /**
     * Returns the cached entry (possibly {@code null} if never fetched), and — throttled by
     * {@link #REFRESH_INTERVAL_TICKS} — fires a request if there's no entry yet or the cached one
     * is old enough to refresh. Safe to call every render frame: the throttle lives here, not in
     * the caller.
     */
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
