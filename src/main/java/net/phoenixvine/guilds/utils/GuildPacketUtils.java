package net.phoenixvine.guilds.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;
import net.phoenixvine.guilds.network.GuildNetwork;
import net.phoenixvine.guilds.network.S2CGuildFlagPacket;
import net.phoenixvine.guilds.network.S2CGuildSyncPacket;

import java.util.*;

import static net.phoenixvine.guilds.utils.GuildUtils.onlineMembers;

public class GuildPacketUtils {

    public static void pushFlagToOnlineMembers(Guild guild, MinecraftServer server) {
        var packet = new S2CGuildFlagPacket(guild.getId(), guild.getFlagIconId(), guild.getFlagPixelData(),
                guild.isFlagUseDrawing(), guild.getFlagWidth(), guild.getFlagHeight());
        for (ServerPlayer member : onlineMembers(guild, server))
            GuildNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> member), packet);
    }

    public static S2CGuildSyncPacket buildPacketFor(ServerPlayer player, GuildManager guildManager) {
        Optional<Guild> opt = guildManager.getGuildFor(player.getUUID());
        var server = Objects.requireNonNull(player.getServer());

        String guildName = null;
        UUID ownerUUID = null;
        String motd = "";
        String description = "";
        boolean friendlyFire = false;
        boolean homeSet = false;
        String flagIconId = "";
        String flagPixelData = "0".repeat(Guild.FLAG_PIXEL_DATA_LENGTH);
        boolean flagUseDrawing = false;
        int flagWidth = Guild.DEFAULT_FLAG_SIZE;
        int flagHeight = Guild.DEFAULT_FLAG_SIZE;
        List<S2CGuildSyncPacket.MemberEntry> members = List.of();
        List<S2CGuildSyncPacket.AllyEntry> allies = List.of();
        List<S2CGuildSyncPacket.PendingEntry> pendingOutgoing = List.of();
        List<S2CGuildSyncPacket.PendingEntry> pendingIncoming = List.of();
        List<S2CGuildSyncPacket.LogEntry> logEntries = List.of();
        List<S2CGuildSyncPacket.WikiPage> wikiPageList = List.of();

        if (opt.isPresent()) {
            var guild = opt.get();
            guildName = guild.getName();
            ownerUUID = guild.getOwner();
            motd = guild.getMotd();
            description = guild.getDescription();
            friendlyFire = guild.isFriendlyFire();
            homeSet = guild.isHomeSet();
            flagIconId = guild.getFlagIconId();
            flagPixelData = guild.getFlagPixelData();
            flagUseDrawing = guild.isFlagUseDrawing();
            flagWidth = guild.getFlagWidth();
            flagHeight = guild.getFlagHeight();

            List<S2CGuildSyncPacket.MemberEntry> memberEntries = new ArrayList<>();
            for (UUID uuid : guild.getMembers()) {
                ServerPlayer online = server.getPlayerList().getPlayer(uuid);
                String name = online != null ? online.getName().getString() : uuid.toString().substring(0, 8) + "…";
                memberEntries.add(
                        new S2CGuildSyncPacket.MemberEntry(uuid, name, online != null, guild.getRank(uuid).name()));
            }
            members = memberEntries;

            List<S2CGuildSyncPacket.AllyEntry> allyEntries = new ArrayList<>();
            for (UUID allyId : guild.getAllies()) {
                guildManager.getGuildById(allyId).ifPresent(ally -> {
                    int onlineCount = onlineMembers(ally, server).size();
                    allyEntries.add(
                            new S2CGuildSyncPacket.AllyEntry(ally.getName(), ally.getMembers().size(), onlineCount));
                });
            }
            allies = allyEntries;

            List<S2CGuildSyncPacket.PendingEntry> outgoingEntries = new ArrayList<>();
            for (UUID pid : guild.getPendingOutgoing()) {
                guildManager.getGuildById(pid)
                        .ifPresent(t -> outgoingEntries.add(new S2CGuildSyncPacket.PendingEntry(t.getName())));
            }
            pendingOutgoing = outgoingEntries;

            List<S2CGuildSyncPacket.PendingEntry> incomingEntries = new ArrayList<>();
            for (Guild other : guildManager.getAllGuilds()) {
                if (!other.getId().equals(guild.getId()) && other.hasPendingOutgoing(guild.getId())) {
                    incomingEntries.add(new S2CGuildSyncPacket.PendingEntry(other.getName()));
                }
            }
            pendingIncoming = incomingEntries;

            List<S2CGuildSyncPacket.LogEntry> loggedEntries = new ArrayList<>();
            for (Guild.LogEntry e : guild.getLog()) {
                loggedEntries.add(new S2CGuildSyncPacket.LogEntry(e.timestamp(), e.message()));
            }
            logEntries = loggedEntries;

            List<S2CGuildSyncPacket.WikiPage> wikiEntries = new ArrayList<>();
            for (Map.Entry<String, String> e : guild.getWikiPages().entrySet()) {
                wikiEntries.add(new S2CGuildSyncPacket.WikiPage(e.getKey(), e.getValue()));
            }
            wikiPageList = wikiEntries;
        }

        List<S2CGuildSyncPacket.GuildSummary> summaries = new ArrayList<>();
        for (Guild g : guildManager.getAllGuilds()) {
            int onlineCount = onlineMembers(g, server).size();
            summaries.add(new S2CGuildSyncPacket.GuildSummary(
                    g.getId(), g.getName(), g.getMembers().size(), onlineCount, g.getDescription()));
        }

        return new S2CGuildSyncPacket(guildName, ownerUUID, motd, description, friendlyFire, homeSet, flagIconId,
                flagPixelData, flagUseDrawing, flagWidth, flagHeight, members, allies, pendingOutgoing,
                pendingIncoming, logEntries, wikiPageList, summaries);
    }

    public static void syncToGuild(Guild guild, MinecraftServer server, GuildManager mgr) {
        for (UUID uuid : guild.getMembers()) {
            ServerPlayer m = server.getPlayerList().getPlayer(uuid);
            if (m != null) syncToPlayer(m, mgr);
        }
    }

    public static void syncToPlayer(ServerPlayer player, GuildManager mgr) {
        GuildNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), buildPacketFor(player, mgr));
    }
}
