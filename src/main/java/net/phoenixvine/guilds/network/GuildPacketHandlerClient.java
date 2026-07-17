package net.phoenixvine.guilds.network;

import net.minecraft.client.Minecraft;
import net.phoenixvine.guilds.client.ClientGuildCache;
import net.phoenixvine.guilds.client.ClientGuildFlagCache;
import net.phoenixvine.guilds.client.GuildScreen;

public class GuildPacketHandlerClient {

    public static void handleSyncPacket(S2CGuildSyncPacket pkt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ClientGuildCache.update(
                pkt.getGuildName(), pkt.getOwnerUUID(), pkt.getMotd(), pkt.getDescription(),
                pkt.isFriendlyFire(), pkt.isHomeSet(), pkt.getFlagIconId(), pkt.getFlagPixelData(),
                pkt.isFlagUseDrawing(), pkt.getFlagWidth(), pkt.getFlagHeight(), pkt.getMembers(),
                pkt.getAllies(), pkt.getPendingOutgoing(), pkt.getPendingIncoming(),
                pkt.getLogEntries(), pkt.getWikiPages(), pkt.getAllGuilds());

        if (mc.screen instanceof GuildScreen gs) {
            gs.onDataRefreshed();
        }
    }

    public static void handleOpenScreenPacket() {
        Minecraft.getInstance().setScreen(new GuildScreen());
    }

    public static void handleClientFlagPacket(S2CGuildFlagPacket pkt) {
        if (Minecraft.getInstance().player == null) return;
        ClientGuildFlagCache.put(
                pkt.getGuildId(), pkt.getFlagIconId(), pkt.getFlagPixelData(), pkt.isFlagUseDrawing(),
                pkt.getFlagWidth(), pkt.getFlagHeight());
    }
}
