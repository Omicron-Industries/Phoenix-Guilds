package net.phoenixvine.guilds.command;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.guilds.PhoenixGuilds;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;
import net.phoenixvine.guilds.event.GuildActions;

import com.mojang.brigadier.arguments.StringArgumentType;

import java.util.Optional;
import java.util.UUID;

import static net.phoenixvine.guilds.event.GuildActions.*;
import static net.phoenixvine.guilds.utils.GuildUtils.*;

@Mod.EventBusSubscriber(modid = PhoenixGuilds.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GuildsCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var d = event.getDispatcher();

        d.register(Commands.literal("guilds")
                .executes(ctx -> openGui(ctx.getSource().getPlayerOrException()))
                .then(Commands.literal("gui").executes(ctx -> openGui(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    handleCreate(ctx.getSource().getPlayerOrException(),
                                            get(ctx.getSource().getPlayerOrException()),
                                            StringArgumentType.getString(ctx, "name"));
                                    return 1;
                                })))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    GuildActions.handleInvite(ctx.getSource().getPlayerOrException(),
                                            get(ctx.getSource().getPlayerOrException()),
                                            EntityArgument.getPlayer(ctx, "player").getName().getString());
                                    return 1;
                                })))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    handleRemove(ctx.getSource().getPlayerOrException(),
                                            get(ctx.getSource().getPlayerOrException()),
                                            EntityArgument.getPlayer(ctx, "player").getName().getString());
                                    return 1;
                                })))
                .then(Commands.literal("leave")
                        .executes(ctx -> {
                            handleLeave(ctx.getSource().getPlayerOrException(),
                                    get(ctx.getSource().getPlayerOrException()));
                            return 1;
                        }))
                .then(Commands.literal("disband")
                        .executes(ctx -> {
                            handleDisband(ctx.getSource().getPlayerOrException(),
                                    get(ctx.getSource().getPlayerOrException()));
                            return 1;
                        }))
                .then(Commands.literal("promote")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    handlePromote(ctx.getSource().getPlayerOrException(),
                                            get(ctx.getSource().getPlayerOrException()),
                                            EntityArgument.getPlayer(ctx, "player").getName().getString());
                                    return 1;
                                })))
                .then(Commands.literal("demote")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    handleDemote(ctx.getSource().getPlayerOrException(),
                                            get(ctx.getSource().getPlayerOrException()),
                                            EntityArgument.getPlayer(ctx, "player").getName().getString());
                                    return 1;
                                })))
                .then(Commands.literal("transfer")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    handleTransfer(ctx.getSource().getPlayerOrException(),
                                            get(ctx.getSource().getPlayerOrException()),
                                            EntityArgument.getPlayer(ctx, "player").getName().getString());
                                    return 1;
                                })))
                .then(Commands.literal("motd")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    handleSetMotd(ctx.getSource().getPlayerOrException(),
                                            get(ctx.getSource().getPlayerOrException()),
                                            StringArgumentType.getString(ctx, "text"));
                                    return 1;
                                })))
                .then(Commands.literal("desc")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    handleSetDesc(ctx.getSource().getPlayerOrException(),
                                            get(ctx.getSource().getPlayerOrException()),
                                            StringArgumentType.getString(ctx, "text"));
                                    return 1;
                                })))
                .then(Commands.literal("friendlyfire")
                        .executes(ctx -> {
                            handleToggleFF(ctx.getSource().getPlayerOrException(),
                                    get(ctx.getSource().getPlayerOrException()));
                            return 1;
                        }))
                .then(Commands.literal("sethome")
                        .executes(ctx -> {
                            handleSetHome(ctx.getSource().getPlayerOrException(),
                                    get(ctx.getSource().getPlayerOrException()));
                            return 1;
                        }))
                .then(Commands.literal("home")
                        .executes(ctx -> {
                            handleHome(ctx.getSource().getPlayerOrException(),
                                    get(ctx.getSource().getPlayerOrException()));
                            return 1;
                        }))
                .then(Commands.literal("info")
                        .executes(ctx -> {
                            cmdInfo(ctx.getSource().getPlayerOrException(),
                                    get(ctx.getSource().getPlayerOrException()));
                            return 1;
                        }))
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            cmdList(ctx.getSource().getPlayerOrException(),
                                    get(ctx.getSource().getPlayerOrException()));
                            return 1;
                        }))
                .then(Commands.literal("log")
                        .executes(ctx -> {
                            cmdLog(ctx.getSource().getPlayerOrException(), get(ctx.getSource().getPlayerOrException()));
                            return 1;
                        }))
                .then(Commands.literal("wiki")
                        .executes(ctx -> openGui(ctx.getSource().getPlayerOrException()))
                        .then(Commands.literal("delete")
                                .then(Commands.argument("title", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            handleWikiDelete(ctx.getSource().getPlayerOrException(),
                                                    get(ctx.getSource().getPlayerOrException()),
                                                    StringArgumentType.getString(ctx, "title"));
                                            return 1;
                                        }))))
                .then(Commands.literal("ally")
                        .then(Commands.literal("request")
                                .then(Commands.argument("guild", StringArgumentType.word())
                                        .executes(ctx -> {
                                            handleAllyRequest(ctx.getSource().getPlayerOrException(),
                                                    get(ctx.getSource().getPlayerOrException()),
                                                    StringArgumentType.getString(ctx, "guild"));
                                            return 1;
                                        })))
                        .then(Commands.literal("accept")
                                .then(Commands.argument("guild", StringArgumentType.word())
                                        .executes(ctx -> {
                                            handleAllyAccept(ctx.getSource().getPlayerOrException(),
                                                    get(ctx.getSource().getPlayerOrException()),
                                                    StringArgumentType.getString(ctx, "guild"));
                                            return 1;
                                        })))
                        .then(Commands.literal("decline")
                                .then(Commands.argument("guild", StringArgumentType.word())
                                        .executes(ctx -> {
                                            handleAllyDecline(ctx.getSource().getPlayerOrException(),
                                                    get(ctx.getSource().getPlayerOrException()),
                                                    StringArgumentType.getString(ctx, "guild"));
                                            return 1;
                                        })))
                        .then(Commands.literal("break")
                                .then(Commands.argument("guild", StringArgumentType.word())
                                        .executes(ctx -> {
                                            handleAllyBreak(ctx.getSource().getPlayerOrException(),
                                                    get(ctx.getSource().getPlayerOrException()),
                                                    StringArgumentType.getString(ctx, "guild"));
                                            return 1;
                                        })))));

        d.register(Commands.literal("gc")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            handleGuildChat(ctx.getSource().getPlayerOrException(),
                                    get(ctx.getSource().getPlayerOrException()),
                                    StringArgumentType.getString(ctx, "message"));
                            return 1;
                        })));

        d.register(Commands.literal("ac")
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            handleAllyChat(ctx.getSource().getPlayerOrException(),
                                    get(ctx.getSource().getPlayerOrException()),
                                    StringArgumentType.getString(ctx, "message"));
                            return 1;
                        })));
    }

    private static void cmdList(ServerPlayer player, GuildManager mgr) {
        var all = mgr.getAllGuilds();
        if (all.isEmpty()) {
            send(player, "§7No Guilds exist yet.");
            return;
        }
        StringBuilder sb = new StringBuilder("§6[Guilds]");
        for (Guild g : all)
            sb.append("\n  §f").append(g.getName()).append(" §7(").append(g.getMembers().size()).append(" members)");
        player.sendSystemMessage(Component.literal(sb.toString()));
    }

    private static void cmdLog(ServerPlayer player, GuildManager mgr) {
        Optional<Guild> opt = mgr.getGuildFor(player.getUUID());
        if (opt.isEmpty()) {
            send(player, "§cYou are not in a Guild.");
            return;
        }
        Guild g = opt.get();
        if (g.getLog().isEmpty()) {
            send(player, "§7No events logged yet.");
            return;
        }
        StringBuilder sb = new StringBuilder("§6[Guild Log] §f" + g.getName());
        for (Guild.LogEntry e : g.getLog()) {
            String time = new java.text.SimpleDateFormat("MM/dd HH:mm").format(new java.util.Date(e.timestamp()));
            sb.append("\n§7").append(time).append(" §f").append(e.message());
        }
        player.sendSystemMessage(Component.literal(sb.toString()));
    }

    private static void cmdInfo(ServerPlayer player, GuildManager mgr) {
        Optional<Guild> opt = mgr.getGuildFor(player.getUUID());
        if (opt.isEmpty()) {
            send(player, "§cYou are not in a Guild.");
            return;
        }
        Guild g = opt.get();
        StringBuilder sb = new StringBuilder("§6[Guild] §f" + g.getName());
        if (!g.getDescription().isBlank()) sb.append("\n§7").append(g.getDescription());
        if (!g.getMotd().isBlank()) sb.append("\n§6MOTD: §f").append(g.getMotd());
        sb.append("\n§7FF: ").append(g.isFriendlyFire() ? "§aON" : "§cOFF");
        sb.append("  Home: ").append(g.isHomeSet() ? "§aSet" : "§cNot set");
        sb.append("\n§7Members:");
        for (UUID m : g.getMembers()) {
            ServerPlayer on = player.getServer().getPlayerList().getPlayer(m);
            String nm = on != null ? on.getName().getString() : m.toString().substring(0, 8) + "…";
            sb.append("\n  ").append(g.getRank(m).display()).append(" §f").append(nm)
                    .append(on != null ? " §a(online)" : " §8(offline)");
        }
        if (!g.getAllies().isEmpty()) {
            sb.append("\n§7Allies:");
            for (UUID aid : g.getAllies())
                mgr.getGuildById(aid).ifPresent(a -> sb.append("\n  §b⚑ §f").append(a.getName()));
        }
        player.sendSystemMessage(Component.literal(sb.toString()));
    }
}
