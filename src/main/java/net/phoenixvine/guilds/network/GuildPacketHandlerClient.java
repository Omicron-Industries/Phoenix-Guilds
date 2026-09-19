package net.phoenixvine.guilds.network;

import net.minecraft.client.Minecraft;
import net.phoenixvine.guilds.client.render.ClientGuildCache;
import net.phoenixvine.guilds.client.render.ClientGuildFlagCache;
import net.phoenixvine.guilds.client.screen.GuildScreen;

public class GuildPacketHandlerClient {

    public static void handleSyncPacket(S2CGuildSyncPacket pkt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ClientGuildCache.update(
                pkt.guildName(), pkt.ownerUUID(), pkt.motd(), pkt.description(),
                pkt.friendlyFire(), pkt.homeSet(), pkt.flagIconId(), pkt.flagPixelData(),
                pkt.flagUseDrawing(), pkt.flagWidth(), pkt.flagHeight(), pkt.members(),
                pkt.allies(), pkt.pendingOutgoing(), pkt.pendingIncoming(),
                pkt.logEntries(), pkt.wikiPages(), pkt.allGuilds());

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
                pkt.guildId(), pkt.flagIconId(), pkt.flagPixelData(), pkt.flagUseDrawing(),
                pkt.flagWidth(), pkt.flagHeight());
    }

    public static void handleStatusPacket(S2CGuildStatusPacket pkt) {
        if (Minecraft.getInstance().player == null) return;
        if (Minecraft.getInstance().screen instanceof GuildScreen gs) {
            gs.showStatus(pkt.message(), pkt.error());
        }
    }
}
