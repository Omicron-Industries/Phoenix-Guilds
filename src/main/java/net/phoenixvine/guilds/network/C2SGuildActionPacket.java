package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.phoenixvine.guilds.data.GuildAction;
import net.phoenixvine.guilds.data.GuildManager;

import java.util.Objects;
import java.util.function.Supplier;

import static net.phoenixvine.guilds.event.GuildActions.*;

public record C2SGuildActionPacket(GuildAction action, String arg) {

    public C2SGuildActionPacket(FriendlyByteBuf buf) {
        this(buf.readEnum(GuildAction.class), buf.readUtf(GuildNetworkLimits.ACTION_ARG_MAX));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUtf(arg, GuildNetworkLimits.ACTION_ARG_MAX);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            GuildManager mgr = GuildManager.get(Objects.requireNonNull(player.getServer()).overworld());
            switch (action) {
                case CREATE -> handleCreate(player, mgr, arg);
                case INVITE -> handleInvite(player, mgr, arg);
                case REMOVE -> handleRemove(player, mgr, arg);
                case LEAVE -> handleLeave(player, mgr);
                case DISBAND -> handleDisband(player, mgr);
                case PROMOTE -> handlePromote(player, mgr, arg);
                case DEMOTE -> handleDemote(player, mgr, arg);
                case TRANSFER -> handleTransfer(player, mgr, arg);
                case SET_MOTD -> handleSetMotd(player, mgr, arg);
                case SET_DESC -> handleSetDesc(player, mgr, arg);
                case TOGGLE_FF -> handleToggleFF(player, mgr);
                case SET_HOME -> handleSetHome(player, mgr);
                case HOME -> handleHome(player, mgr);
                case ALLY_REQUEST -> handleAllyRequest(player, mgr, arg);
                case ALLY_ACCEPT -> handleAllyAccept(player, mgr, arg);
                case ALLY_DECLINE -> handleAllyDecline(player, mgr, arg);
                case ALLY_BREAK -> handleAllyBreak(player, mgr, arg);
                case GUILD_CHAT -> handleGuildChat(player, mgr, arg);
                case ALLY_CHAT -> handleAllyChat(player, mgr, arg);
                case WIKI_SET -> handleWikiSet(player, mgr, arg);
                case WIKI_DELETE -> handleWikiDelete(player, mgr, arg);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
