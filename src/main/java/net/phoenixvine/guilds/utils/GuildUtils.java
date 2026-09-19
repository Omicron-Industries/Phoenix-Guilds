package net.phoenixvine.guilds.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;
import net.phoenixvine.guilds.network.GuildNetwork;
import net.phoenixvine.guilds.network.S2CGuildStatusPacket;
import net.phoenixvine.guilds.network.S2COpenGuildScreenPacket;

import java.util.List;
import java.util.Objects;

public class GuildUtils {

    public static void send(ServerPlayer player, String msg) {
        player.sendSystemMessage(Component.literal(msg));
    }

    public static void sendStatus(ServerPlayer player, String message, boolean error) {
        send(player, (error ? "§c" : "§a") + message);
        GuildNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new S2CGuildStatusPacket(message, error));
    }

    public static int openGui(ServerPlayer player) {
        GuildNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new S2COpenGuildScreenPacket());
        return 1;
    }

    public static GuildManager get(ServerPlayer player) {
        return GuildManager.get(Objects.requireNonNull(player.getServer()).overworld());
    }

    public static List<ServerPlayer> onlineMembers(Guild g, MinecraftServer server) {
        return g.getMembers().stream().map(u -> server.getPlayerList().getPlayer(u)).filter(Objects::nonNull).toList();
    }

    public static boolean isValidFlagIcon(String iconId) {
        if (iconId == null || iconId.isBlank()) return true;

        if (iconId.startsWith("item:")) {
            var id = ResourceLocation.tryParse(iconId.substring(5));
            return id != null && ForgeRegistries.ITEMS.containsKey(id);
        }

        if (iconId.startsWith("block:")) {
            var id = ResourceLocation.tryParse(iconId.substring(6));
            return id != null && ForgeRegistries.BLOCKS.containsKey(id);
        }

        return false;
    }
}
