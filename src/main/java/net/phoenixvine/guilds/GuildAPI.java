package net.phoenixvine.guilds;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;
import net.phoenixvine.guilds.data.GuildRank;

import java.util.*;

@SuppressWarnings("all")
public final class GuildAPI {

    private GuildAPI() {}

    /**
     * Checks if a player is in a guild.
     * 
     * @return false if the player is not a guild.
     */
    public static boolean isInGuild(UUID playerUUID) {
        var mgr = manager();
        return mgr != null && mgr.isInGuild(playerUUID);
    }

    /**
     * Gets the name of the guild for the chosen player.
     * 
     * @return an empty Optional if the player is not in a guild.
     */
    public static Optional<String> getGuildName(UUID playerUUID) {
        var mgr = manager();
        if (mgr == null) return Optional.empty();
        return mgr.getGuildFor(playerUUID).map(Guild::getName);
    }

    /**
     * Gets the UUID of the guild the chosen player is in.
     * 
     * @return an empty Optional if the player is not in a guild.
     */
    public static Optional<UUID> getGuildId(UUID playerUUID) {
        var mgr = manager();
        if (mgr == null) return Optional.empty();
        return mgr.getGuildFor(playerUUID).map(Guild::getId);
    }

    /**
     * Gets an unmodifiable list of members in the player's guild.
     * 
     * @return an empty collection if the player is not in a guild.
     */
    public static Set<UUID> getGuildMembers(UUID playerUUID) {
        GuildManager mgr = manager();
        if (mgr == null) return Collections.emptySet();
        return mgr.getGuildFor(playerUUID)
                .map(g -> Collections.unmodifiableSet(g.getMembers()))
                .orElse(Collections.emptySet());
    }

    /**
     * Gets a list of online members in the player's guild.
     * 
     * @return an empty collection if the player is not in a guild.
     */
    public static List<ServerPlayer> getOnlineGuildMembers(UUID playerUUID) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Collections.emptyList();
        GuildManager mgr = GuildManager.get(server.overworld());
        return mgr.getGuildFor(playerUUID)
                .map(g -> g.getMembers().stream()
                        .map(server.getPlayerList()::getPlayer)
                        .filter(Objects::nonNull)
                        .toList())
                .orElse(Collections.emptyList());
    }

    /**
     * Checks if two players are in the same guild.
     * 
     * @return false if either of the players is either not guildmates or does not exist.
     */
    public static boolean areGuildmates(UUID a, UUID b) {
        if (a.equals(b)) return false;
        GuildManager mgr = manager();
        if (mgr == null) return false;
        Optional<Guild> ga = mgr.getGuildFor(a);
        Optional<Guild> gb = mgr.getGuildFor(b);
        return ga.isPresent() && gb.isPresent() && ga.get().getId().equals(gb.get().getId());
    }

    /**
     * Checks if two players are in the same guild.
     * 
     * @return false if either of the players is either not allied or does not exist.
     */
    public static boolean areAllied(UUID a, UUID b) {
        GuildManager mgr = manager();
        if (mgr == null) return false;
        Optional<Guild> ga = mgr.getGuildFor(a);
        Optional<Guild> gb = mgr.getGuildFor(b);
        if (ga.isEmpty() || gb.isEmpty()) return false;
        if (ga.get().getId().equals(gb.get().getId())) return false;
        return ga.get().isAlly(gb.get().getId());
    }

    /**
     * Checks if two players are friendly.
     * 
     * @return false if either areGuildmatees or areAllied fail to match.
     */
    public static boolean areFriendly(UUID a, UUID b) {
        return areGuildmates(a, b) || areAllied(a, b);
    }

    /**
     * Gets the rank (OWNER, OFFICER, or MEMBER) of the selected player.
     * 
     * @return an empty optional if the player is not in a guild.
     */
    public static Optional<GuildRank> getGuildRank(UUID playerUUID) {
        GuildManager mgr = manager();
        if (mgr == null) return Optional.empty();
        return mgr.getGuildFor(playerUUID).map(g -> g.getRank(playerUUID));
    }

    /**
     * Checks to see if the player has a specific guild rank.
     * 
     * @return false if the player does not have that rank.
     */
    public static boolean hasRank(UUID playerUUID, GuildRank required) {
        return getGuildRank(playerUUID).map(r -> r.isAtLeast(required)).orElse(false);
    }

    /**
     * Checks to see if the player is the owner of the guild.
     * 
     * @return false if the player is not.
     */
    public static boolean isOwner(UUID playerUUID) {
        return hasRank(playerUUID, GuildRank.OWNER);
    }

    /**
     * Checks to see if the player is an owner/officer of the guild.
     * 
     * @return false if the player is not.
     */
    public static boolean isOfficerOrAbove(UUID playerUUID) {
        return hasRank(playerUUID, GuildRank.OFFICER);
    }

    /**
     * Fetches the guild name directly through it's UIID.
     * 
     * @return an empty optional if that guild does not exist.
     */
    public static Optional<String> getGuildNameById(UUID guildId) {
        GuildManager mgr = manager();
        if (mgr == null) return Optional.empty();
        return mgr.getGuildById(guildId).map(Guild::getName);
    }

    /**
     * Fetches the UUID of all players in a specified guild.
     * 
     * @return an empty collection if that guild does not exist.
     */
    public static Set<UUID> getGuildMembersById(UUID guildId) {
        GuildManager mgr = manager();
        if (mgr == null) return Collections.emptySet();
        return mgr.getGuildById(guildId)
                .map(g -> Collections.unmodifiableSet(g.getMembers()))
                .orElse(Collections.emptySet());
    }

    /**
     * Resolves a token of either a guild's UIID or the player's as a fallback.
     * 
     * @return null if nether a guild or a player exist .
     */
    public static UUID getGuildIdOrPlayerFallback(UUID playerUUID) {
        if (playerUUID == null) return null;
        GuildManager mgr = manager();
        if (mgr == null) return playerUUID;
        return mgr.getGuildFor(playerUUID)
                .map(Guild::getId)
                .orElse(playerUUID);
    }

    /**
     * Checks to see if the player resolves a target token directly or if they are in a guild that matches that token.
     * 
     * @return false if the guild, token, and/or player do not exist.
     */
    public static boolean isPlayerInGuildOrIs(UUID playerUUID, UUID token) {
        if (playerUUID == null || token == null) return false;
        if (playerUUID.equals(token)) return true;
        GuildManager mgr = manager();
        if (mgr == null) return false;
        return mgr.getGuildById(token)
                .map(g -> g.isMember(playerUUID))
                .orElse(false);
    }

    /**
     * Attempts to resolve a name for the given token.
     * It checks if it's a guild first and if not, falls back to player name.
     * 
     * @return the token "Unknown" if both the guild and playername cannot resolve.
     */
    public static String getDisplayName(UUID guildOrPlayerToken) {
        if (guildOrPlayerToken == null) return "Unknown";
        GuildManager mgr = manager();
        if (mgr != null) {
            Optional<String> guildName = mgr.getGuildById(guildOrPlayerToken).map(Guild::getName);
            if (guildName.isPresent()) return guildName.get();
        }
        return resolvePlayerName(guildOrPlayerToken);
    }

    /**
     * Attempts to find the player's display name through online players or the server's cache.
     * 
     * @return a truncated player UUID if the name is not available.
     */
    public static String resolvePlayerName(UUID playerUUID) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerPlayer online = server.getPlayerList().getPlayer(playerUUID);
            if (online != null) return online.getGameProfile().getName();

            if (server.getProfileCache() != null) {
                Optional<com.mojang.authlib.GameProfile> cached = server.getProfileCache().get(playerUUID);
                if (cached.isPresent()) return cached.get().getName();
            }
        }
        return "Player: " + playerUUID.toString().substring(0, 8);
    }

    /**
     * Resolves a standard string identifier for a player's guild context.
     * Useful for external mods looking to query guild affiliation.
     *
     * @return an Optional containing the formatted guild identifier
     *         or an empty if the player is not in a guild or the server is unavailable.
     */
    public static Optional<String> getGuildIdentifierString(ServerPlayer player) {
        if (player == null) return Optional.empty();
        MinecraftServer server = player.getServer();
        if (server == null) return Optional.empty();

        GuildManager guildMgr = GuildManager.get(server.overworld());
        if (guildMgr == null) return Optional.empty();

        return guildMgr.getGuildFor(player.getUUID())
                .map(guild -> "guild:" + guild.getId());
    }

    /**
     * Overload taking a player UUID directly, utilizing the global server lifecycle hook.
     */
    public static Optional<String> getGuildIdentifierString(UUID playerUUID) {
        if (playerUUID == null) return Optional.empty();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Optional.empty();

        GuildManager guildMgr = GuildManager.get(server.overworld());
        if (guildMgr == null) return Optional.empty();

        return guildMgr.getGuildFor(playerUUID)
                .map(guild -> "guild:" + guild.getId());
    }

    private static GuildManager manager() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        return GuildManager.get(server.overworld());
    }
}
