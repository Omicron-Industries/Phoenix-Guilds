package net.phoenixvine.guilds.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;
import net.phoenixvine.guilds.data.GuildRank;
import net.phoenixvine.guilds.utils.GuildContextAction;
import net.phoenixvine.guilds.utils.GuildPacketUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.phoenixvine.guilds.integration.gtceu.GuildsGTCEuIntegration.migrateGTCEuOwnership;
import static net.phoenixvine.guilds.utils.GuildPacketUtils.*;
import static net.phoenixvine.guilds.utils.GuildUtils.*;

public class GuildActions {

    private static final Map<UUID, Long> HOME_COOLDOWNS = new ConcurrentHashMap<>();
    private static final long HOME_COOLDOWN_MS = 5 * 60 * 1000L;

    public static void handleCreate(ServerPlayer player, GuildManager mgr, String name) {
        if (name.isBlank()) {
            send(player, "§cGuild name cannot be empty.");
            return;
        }
        if (mgr.isInGuild(player.getUUID())) {
            send(player, "§cYou are already in a Guild.");
            return;
        }
        if (mgr.getGuildByName(name).isPresent()) {
            send(player, "§cA Guild named '§f" + name + "§c' already exists.");
            return;
        }
        var guild = mgr.createGuild(name, player.getUUID());
        send(player, "§aCreated Guild §f" + guild.getName() + ".");
        migrateGTCEuOwnership(player.getUUID(), guild.getId());
        syncToPlayer(player, mgr);
        broadcastAllGuildList(Objects.requireNonNull(player.getServer()), mgr);
    }

    public static void broadcastAllGuildList(MinecraftServer server, GuildManager mgr) {
        for (ServerPlayer p : server.getPlayerList().getPlayers())
            if (!mgr.isInGuild(p.getUUID())) syncToPlayer(p, mgr);
    }

    public static void handleInvite(ServerPlayer player, GuildManager mgr, String targetName) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            if (!guild.hasRank(player.getUUID(), GuildRank.OFFICER)) {
                send(player, "§cOfficers and above can invite players.");
                return;
            }
            ServerPlayer target = Objects.requireNonNull(player.getServer()).getPlayerList()
                    .getPlayerByName(targetName);
            if (target == null) {
                send(player, "§cPlayer '§f" + targetName + "§c' is not online.");
                return;
            }
            if (mgr.isInGuild(target.getUUID())) {
                send(player, "§c" + targetName + " is already in a Guild.");
                return;
            }
            if (guild.isFull()) {
                send(player, "§cYour Guild is full (§f" + Guild.MAX_MEMBERS + "§c members max).");
                return;
            }
            mgr.addMember(guild.getId(), target.getUUID());
            guild.addLog(player.getName().getString() + " invited " + targetName + ".");
            send(player, "§aAdded §f" + targetName + " §ato §f" + guild.getName() + "§a.");
            target.sendSystemMessage(Component.literal(
                    "§aYou were added to guild §f" + guild.getName() + " §aby §f" + player.getName().getString() +
                            "§a."));
            migrateGTCEuOwnership(target.getUUID(), guild.getId());
            syncToGuild(guild, player.getServer(), mgr);
            broadcastAllGuildList(player.getServer(), mgr);
        });
    }

    public static void handleHome(ServerPlayer player, GuildManager mgr) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            if (!guild.isHomeSet()) {
                send(player, "§cGuild home has not been set yet.");
                return;
            }

            long now = System.currentTimeMillis();
            Long expires = HOME_COOLDOWNS.get(player.getUUID());
            if (expires != null && now < expires) {
                long secsLeft = (expires - now) / 1000;
                send(player, "§cGuild home is on cooldown for §f" + secsLeft + "s§c.");
                return;
            }
            HOME_COOLDOWNS.put(player.getUUID(), now + HOME_COOLDOWN_MS);

            ServerLevel targetLevel = Objects.requireNonNull(player.getServer()).getLevel(
                    ResourceKey.create(Registries.DIMENSION, guild.getHomeDimension()));
            if (targetLevel == null) {
                send(player, "§cGuild home dimension is no longer loaded.");
                return;
            }
            if (player.level() == targetLevel) {
                player.connection.teleport(guild.getHomeX(), guild.getHomeY(), guild.getHomeZ(), guild.getHomeYaw(),
                        guild.getHomePitch());
            } else {
                player.teleportTo(targetLevel, guild.getHomeX(), guild.getHomeY(), guild.getHomeZ(), guild.getHomeYaw(),
                        guild.getHomePitch());
            }
            send(player, "§aTeleported to §f" + guild.getName() + "§a's home.");
        });
    }

    public static void handlePromote(ServerPlayer player, GuildManager mgr, String targetName) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            ServerPlayer target = Objects.requireNonNull(player.getServer()).getPlayerList()
                    .getPlayerByName(targetName);
            if (target == null || !guild.isMember(target.getUUID())) {
                send(player, "§c" + targetName + " is not in your Guild or is not online.");
                return;
            }
            String result = mgr.promotePlayer(guild.getId(), player.getUUID(), target.getUUID());
            switch (result) {
                case "ok" -> {
                    GuildRank newRank = guild.getRank(target.getUUID());
                    guild.addLog(
                            player.getName().getString() + " promoted " + targetName + " to " + newRank.label() + ".");
                    send(player, "§aPromoted §f" + targetName + " §ato §f" + newRank.label() + "§a.");
                    target.sendSystemMessage(Component
                            .literal("§aYou were promoted to §f" + newRank.label() + " §ain §f" + guild.getName() +
                                    "§a."));
                    syncToGuild(guild, player.getServer(), mgr);
                }
                case "no_permission" -> send(player, "§cOnly the Guild owner can promote players.");
                case "already_owner" -> send(player, "§cUse /guilds transfer to make them owner.");
                default -> send(player, "§cCould not promote: " + result);
            }
        });
    }

    public static void handleDemote(ServerPlayer player, GuildManager mgr, String targetName) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            ServerPlayer target = Objects.requireNonNull(player.getServer()).getPlayerList()
                    .getPlayerByName(targetName);
            if (target == null || !guild.isMember(target.getUUID())) {
                send(player, "§c" + targetName + " is not in your Guild or is not online.");
                return;
            }
            String result = mgr.demotePlayer(guild.getId(), player.getUUID(), target.getUUID());
            switch (result) {
                case "ok" -> {
                    guild.addLog(player.getName().getString() + " demoted " + targetName + " to Member.");
                    send(player, "§7Demoted §f" + targetName + " §7to Member.");
                    target.sendSystemMessage(
                            Component.literal("§7You were demoted to Member in §f" + guild.getName() + "§7."));
                    syncToGuild(guild, player.getServer(), mgr);
                }
                case "no_permission" -> send(player, "§cOnly the Guild owner can demote players.");
                case "cant_demote_owner" -> send(player, "§cCannot demote the owner.");
                case "already_member" -> send(player, "§c" + targetName + " is already a Member.");
                default -> send(player, "§cCould not demote: " + result);
            }
        });
    }

    public static void handleTransfer(ServerPlayer player, GuildManager mgr, String targetName) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            ServerPlayer target = Objects.requireNonNull(player.getServer()).getPlayerList()
                    .getPlayerByName(targetName);
            if (target == null || !guild.isMember(target.getUUID())) {
                send(player, "§c" + targetName + " is not in your Guild or is not online.");
                return;
            }
            String result = mgr.transferOwnership(guild.getId(), player.getUUID(), target.getUUID());
            if ("ok".equals(result)) {
                guild.addLog(player.getName().getString() + " transferred ownership to " + targetName + ".");
                send(player, "§aTransferred ownership of §f" + guild.getName() + " §ato §f" + targetName + "§a.");
                target.sendSystemMessage(
                        Component.literal("§aYou are now the owner of Guild §f" + guild.getName() + "§a."));
                syncToGuild(guild, player.getServer(), mgr);
            } else {
                send(player, "§cOnly the Guild owner can transfer ownership.");
            }
        });
    }

    public static void handleRemove(ServerPlayer player, GuildManager mgr, String targetName) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            if (!guild.hasRank(player.getUUID(), GuildRank.OFFICER)) {
                send(player, "§cOfficers and above can remove players.");
                return;
            }
            ServerPlayer target = Objects.requireNonNull(player.getServer()).getPlayerList()
                    .getPlayerByName(targetName);
            if (target == null || !guild.isMember(target.getUUID())) {
                send(player, "§c" + targetName + " is not in your Guild or is not online.");
                return;
            }
            if (guild.getRank(target.getUUID()).ordinal() >= guild.getRank(player.getUUID()).ordinal()) {
                send(player, "§cYou cannot remove someone of equal or higher rank.");
                return;
            }
            mgr.removeMember(guild.getId(), target.getUUID());
            guild.addLog(player.getName().getString() + " removed " + targetName + ".");
            send(player, "§cRemoved §f" + targetName + " §cfrom §f" + guild.getName() + "§c.");
            target.sendSystemMessage(Component.literal("§cYou were removed from Guild §f" + guild.getName() + "§c."));
            syncToGuild(guild, player.getServer(), mgr);
            syncToPlayer(target, mgr);
            GuildActions.broadcastAllGuildList(player.getServer(), mgr);
        });
    }

    public static void handleLeave(ServerPlayer player, GuildManager mgr) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            String name = guild.getName();
            UUID gid = guild.getId();
            guild.addLog(player.getName().getString() + " left the Guild.");
            mgr.removeMember(gid, player.getUUID());
            send(player, "§cLeft Guild §f" + name + "§c.");
            syncToPlayer(player, mgr);
            mgr.getGuildById(gid).ifPresent(s -> syncToGuild(s, player.getServer(), mgr));
            GuildActions.broadcastAllGuildList(Objects.requireNonNull(player.getServer()), mgr);
        });
    }

    public static void handleDisband(ServerPlayer player, GuildManager mgr) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            if (!guild.getOwner().equals(player.getUUID())) {
                send(player, "§cOnly the owner can delete the Guild.");
                return;
            }
            String name = guild.getName();
            List<ServerPlayer> allOnline = onlineMembers(guild, player.getServer());
            for (ServerPlayer mem : allOnline)
                if (!mem.getUUID().equals(player.getUUID()))
                    mem.sendSystemMessage(Component.literal("§cGuild §f" + name + " §cwas deleted."));
            mgr.disbandGuild(guild.getId());
            send(player, "§cDeleted Guild §f" + name + "§c.");
            for (ServerPlayer mem : allOnline) syncToPlayer(mem, mgr);
            GuildActions.broadcastAllGuildList(Objects.requireNonNull(player.getServer()), mgr);
        });
    }

    public static void handleSetMotd(ServerPlayer player, GuildManager mgr, String text) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            if (!mgr.setMotd(guild.getId(), player.getUUID(), text)) {
                send(player, "§cOfficers and above can set the MOTD.");
                return;
            }
            guild.addLog(player.getName().getString() + " updated the MOTD.");
            send(player, "§aGuild MOTD updated.");
            syncToGuild(guild, player.getServer(), mgr);
            GuildActions.broadcastAllGuildList(Objects.requireNonNull(player.getServer()), mgr);
        });
    }

    public static void handleSetDesc(ServerPlayer player, GuildManager mgr, String text) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            if (!mgr.setDescription(guild.getId(), player.getUUID(), text)) {
                send(player, "§cOfficers and above can set the description.");
                return;
            }
            send(player, "§aGuild description updated.");
            syncToGuild(guild, player.getServer(), mgr);
            GuildActions.broadcastAllGuildList(Objects.requireNonNull(player.getServer()), mgr);
        });
    }

    public static void handleToggleFF(ServerPlayer player, GuildManager mgr) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            if (!mgr.toggleFriendlyFire(guild.getId(), player.getUUID())) {
                send(player, "§cOnly the Guild owner can toggle friendly fire.");
                return;
            }
            boolean nowOn = guild.isFriendlyFire();
            guild.addLog(player.getName().getString() + " turned friendly fire " + (nowOn ? "ON" : "OFF") + ".");
            send(player, "§aFriendly fire is now §f" + (nowOn ? "§aON" : "§cOFF") + "§a.");
            syncToGuild(guild, player.getServer(), mgr);
        });
    }

    public static void handleSetHome(ServerPlayer player, GuildManager mgr) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            ResourceLocation dim = player.level().dimension().location();
            boolean ok = mgr.setHome(guild.getId(), player.getUUID(), dim,
                    player.getX(), player.getY(), player.getZ(),
                    player.getYRot(), player.getXRot());
            if (!ok) {
                send(player, "§cOfficers and above can set the Guild home.");
                return;
            }
            guild.addLog(player.getName().getString() + " set Guild home.");
            send(player, "§aGuild home set to your current location.");
            syncToGuild(guild, player.getServer(), mgr);
        });
    }

    public static void handleGuildChat(ServerPlayer player, GuildManager mgr, String message) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            String formatted = "§2[Guild] §a" + player.getName().getString() + "§7: §f" + message;
            Component msg = Component.literal(formatted);
            for (UUID uuid : guild.getMembers()) {
                ServerPlayer member = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayer(uuid);
                if (member != null) member.sendSystemMessage(msg);
            }
        });
    }

    public static void handleAllyChat(ServerPlayer player, GuildManager mgr, String message) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            String formatted = "§3[Ally] §b" + player.getName().getString() + " §7[" + guild.getName() + "]§7: §f" +
                    message;
            Component msg = Component.literal(formatted);

            for (UUID uuid : guild.getMembers()) {
                ServerPlayer member = Objects.requireNonNull(player.getServer()).getPlayerList().getPlayer(uuid);
                if (member != null) member.sendSystemMessage(msg);
            }

            for (UUID allyId : guild.getAllies()) {
                mgr.getGuildById(allyId).ifPresent(ally -> {
                    for (UUID uuid : ally.getMembers()) {
                        ServerPlayer member = Objects.requireNonNull(player.getServer()).getPlayerList()
                                .getPlayer(uuid);
                        if (member != null) member.sendSystemMessage(msg);
                    }
                });
            }
        });
    }

    public static void handleAllyRequest(ServerPlayer player, GuildManager mgr, String targetName) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            if (!guild.hasRank(player.getUUID(), GuildRank.OFFICER)) {
                send(player, "§cOfficers and above can send alliance requests.");
                return;
            }
            String result = mgr.sendAllyRequest(guild.getId(), targetName);
            switch (result) {
                case "sent" -> {
                    sendStatus(player, "Alliance request sent to " + targetName + ".", false);
                    mgr.getGuildByName(targetName).ifPresent(target -> {
                        ServerPlayer to = Objects.requireNonNull(player.getServer()).getPlayerList()
                                .getPlayer(target.getOwner());
                        if (to != null) to.sendSystemMessage(Component.literal("§6[Guild] §f" + guild.getName() +
                                " §6has sent your guild an alliance request. Use §e/guilds ally accept " +
                                guild.getName()));
                        syncToGuild(target, player.getServer(), mgr);
                    });
                    syncToGuild(guild, player.getServer(), mgr);
                }
                case "accepted" -> {
                    sendStatus(player, "Alliance formed with " + targetName + "!", false);
                    mgr.getGuildByName(targetName).ifPresent(t -> syncToGuild(t, player.getServer(), mgr));
                    syncToGuild(guild, player.getServer(), mgr);
                }
                case "already_ally" -> sendStatus(player, "Already allied with " + targetName + ".", true);
                case "already_sent" -> sendStatus(player, "Request already pending.", true);
                case "self" -> sendStatus(player, "You can't ally with your own Guild.", true);
                case "guild_not_found" -> sendStatus(player, "Guild '" + targetName + "' not found.", true);
            }
        });
    }

    public static void handleAllyAccept(ServerPlayer player, GuildManager mgr, String senderName) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            if (!guild.hasRank(player.getUUID(), GuildRank.OFFICER)) {
                send(player, "§cOfficers and above can accept alliance requests.");
                return;
            }
            String result = mgr.acceptAllyRequest(guild.getId(), senderName);
            switch (result) {
                case "accepted" -> {
                    send(player, "§aAlliance formed with §f" + senderName + "§a!");
                    mgr.getGuildByName(senderName).ifPresent(s -> {
                        syncToGuild(s, player.getServer(), mgr);
                        ServerPlayer so = Objects.requireNonNull(player.getServer()).getPlayerList()
                                .getPlayer(s.getOwner());
                        if (so != null) so.sendSystemMessage(
                                Component.literal(
                                        "§a[Guild] §f" + guild.getName() + " §aaccepted your alliance request!"));
                    });
                    syncToGuild(guild, player.getServer(), mgr);
                }
                case "no_request" -> send(player, "§cNo pending request from §f" + senderName + ".");
                case "guild_not_found" -> send(player, "§cGuild '§f" + senderName + "§c' not found.");
            }
        });
    }

    public static void handleAllyDecline(ServerPlayer player, GuildManager mgr, String senderName) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            if (!guild.hasRank(player.getUUID(), GuildRank.OFFICER)) {
                send(player, "§cOfficers and above can decline alliance requests.");
                return;
            }
            String result = mgr.declineAllyRequest(guild.getId(), senderName);
            switch (result) {
                case "declined" -> {
                    send(player, "§7Declined request from §f" + senderName + "§7.");
                    mgr.getGuildByName(senderName).ifPresent(s -> syncToGuild(s, player.getServer(), mgr));
                    syncToGuild(guild, player.getServer(), mgr);
                }
                case "no_request" -> send(player, "§cNo pending request from §f" + senderName + ".");
                case "guild_not_found" -> send(player, "§cGuild '§f" + senderName + "§c' not found.");
            }
        });
    }

    public static void handleAllyBreak(ServerPlayer player, GuildManager mgr, String targetName) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            if (!guild.hasRank(player.getUUID(), GuildRank.OFFICER)) {
                send(player, "§cOfficers and above can break alliances.");
                return;
            }
            String result = mgr.breakAlliance(guild.getId(), targetName);
            switch (result) {
                case "broken" -> {
                    send(player, "§cBroke alliance with §f" + targetName + "§c.");
                    mgr.getGuildByName(targetName).ifPresent(t -> {
                        syncToGuild(t, player.getServer(), mgr);
                        ServerPlayer to = Objects.requireNonNull(player.getServer()).getPlayerList()
                                .getPlayer(t.getOwner());
                        if (to != null) to.sendSystemMessage(
                                Component.literal("§c[Guild] §f" + guild.getName() + " §cbroke the alliance."));
                    });
                    syncToGuild(guild, player.getServer(), mgr);
                }
                case "not_ally" -> send(player, "§cNot allied with §f" + targetName + "§c.");
                case "guild_not_found" -> send(player, "§cGuild '§f" + targetName + "§c' not found.");
            }
        });
    }

    public static void handleWikiSet(ServerPlayer player, GuildManager mgr, String arg) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            int sep = arg.indexOf(' ');
            if (sep < 0) {
                send(player, "§cInvalid wiki data.");
                return;
            }
            String title = arg.substring(0, sep).trim();
            String content = arg.substring(sep + 1);
            String result = mgr.setWikiPage(guild.getId(), player.getUUID(), title, content);
            switch (result) {
                case "ok" -> {
                    send(player, "§aWiki page '§f" + title + "§a' saved.");
                    guild.addLog(player.getName().getString() + " updated wiki page \"" + title + "\".");
                    syncToGuild(guild, player.getServer(), mgr);
                }
                case "no_permission" -> send(player, "§cOfficers and above can edit the wiki.");
                case "wiki_full" -> send(player, "§cWiki is full (20 pages max). Delete a page first.");
                case "empty_title" -> send(player, "§cPage title cannot be empty.");
            }
        });
    }

    public static void handleSetFlag(ServerPlayer player, GuildManager mgr, UUID targetGuildId, boolean useDrawing,
                                     String iconId, String pixelData, int width, int height) {
        Optional<Guild> currentGuild = mgr.getGuildFor(player.getUUID());
        if (targetGuildId == null) {
            if (currentGuild.isEmpty()) {
                send(player, "§cYou are not in a Guild.");
                return;
            }
        } else if (mgr.getGuildById(targetGuildId).isEmpty()) {
            send(player, "§cThat flag's guild no longer exists.");
            return;
        } else if (currentGuild.isEmpty() || !currentGuild.get().getId().equals(targetGuildId)) {
            send(player, "§cYou're not a member of this flag's guild.");
            return;
        }
        Guild g = targetGuildId == null ? currentGuild.get() : mgr.getGuildById(targetGuildId).get();
        if (useDrawing) {
            if (pixelData == null || pixelData.length() != Guild.FLAG_PIXEL_DATA_LENGTH ||
                    !pixelData.matches("[0-9a-f]+")) {
                send(player, "§cInvalid flag drawing data.");
                return;
            }
        } else if (!isValidFlagIcon(iconId)) {
            send(player, "§cThat item or block doesn't exist.");
            return;
        }
        if (!mgr.setFlag(g.getId(), player.getUUID(), useDrawing, iconId, pixelData, width, height)) {
            send(player, "§cOfficers and above can edit the Guild flag.");
            return;
        }
        g.addLog(player.getName().getString() + " updated the Guild flag.");
        send(player, "§aGuild flag updated.");
        syncToGuild(g, player.getServer(), mgr);
        GuildPacketUtils.pushFlagToOnlineMembers(g, player.getServer());
    }

    public static void handleWikiDelete(ServerPlayer player, GuildManager mgr, String title) {
        GuildActions.runWithGuild(player, mgr, (guild) -> {
            String result = mgr.deleteWikiPage(guild.getId(), player.getUUID(), title);
            switch (result) {
                case "ok" -> {
                    send(player, "§7Deleted wiki page '§f" + title + "§7'.");
                    guild.addLog(player.getName().getString() + " deleted wiki page \"" + title + "\".");
                    syncToGuild(guild, player.getServer(), mgr);
                }
                case "no_permission" -> send(player, "§cOfficers and above can delete wiki pages.");
                case "not_found" -> send(player, "§cNo wiki page named '§f" + title + "§c'.");
            }
        });
    }

    public static void runWithGuild(ServerPlayer player, GuildManager mgr, GuildContextAction action) {
        mgr.getGuildFor(player.getUUID()).ifPresentOrElse(
                action::execute,
                () -> send(player, "§cYou are not in a Guild."));
    }
}
