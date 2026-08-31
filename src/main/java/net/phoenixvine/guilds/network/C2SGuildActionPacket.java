package net.phoenixvine.guilds.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.phoenixvine.guilds.PhoenixGuilds;
import net.phoenixvine.guilds.data.GuildManager;
import net.phoenixvine.guilds.event.GuildEvents;

public record C2SGuildActionPacket(Action action, String arg) implements CustomPacketPayload {

    public static final Type<C2SGuildActionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PhoenixGuilds.MOD_ID, "guild_action")
    );

    public static final StreamCodec<ByteBuf, C2SGuildActionPacket> STREAM_CODEC = StreamCodec.composite(
            StreamCodec.of(
                    (buf, act) -> new FriendlyByteBuf(buf).writeEnum(act),
                    buf -> new FriendlyByteBuf(buf).readEnum(Action.class)
            ),
            C2SGuildActionPacket::action,
            ByteBufCodecs.stringUtf8(GuildNetworkLimits.ACTION_ARG_MAX),
            C2SGuildActionPacket::arg,
            C2SGuildActionPacket::new
    );

    @Override
    public Type<C2SGuildActionPacket> type() {
        return TYPE;
    }

    public enum Action {
        CREATE,
        INVITE,
        REMOVE,
        LEAVE,
        DISBAND,
        PROMOTE,
        DEMOTE,
        TRANSFER,
        SET_MOTD,
        SET_DESC,
        TOGGLE_FF,
        SET_HOME,
        HOME,
        ALLY_REQUEST,
        ALLY_ACCEPT,
        ALLY_DECLINE,
        ALLY_BREAK,
        GUILD_CHAT,
        ALLY_CHAT,
        WIKI_SET,
        WIKI_DELETE
    }

    public static void handle(C2SGuildActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            GuildManager mgr = GuildManager.get(player.getServer().overworld());
            switch (packet.action()) {
                case CREATE -> GuildEvents.handleCreate(player, mgr, packet.arg());
                case INVITE -> GuildEvents.handleInvite(player, mgr, packet.arg());
                case REMOVE -> GuildEvents.handleRemove(player, mgr, packet.arg());
                case LEAVE -> GuildEvents.handleLeave(player, mgr);
                case DISBAND -> GuildEvents.handleDisband(player, mgr);
                case PROMOTE -> GuildEvents.handlePromote(player, mgr, packet.arg());
                case DEMOTE -> GuildEvents.handleDemote(player, mgr, packet.arg());
                case TRANSFER -> GuildEvents.handleTransfer(player, mgr, packet.arg());
                case SET_MOTD -> GuildEvents.handleSetMotd(player, mgr, packet.arg());
                case SET_DESC -> GuildEvents.handleSetDesc(player, mgr, packet.arg());
                case TOGGLE_FF -> GuildEvents.handleToggleFF(player, mgr);
                case SET_HOME -> GuildEvents.handleSetHome(player, mgr);
                case HOME -> GuildEvents.handleHome(player, mgr);
                case ALLY_REQUEST -> GuildEvents.handleAllyRequest(player, mgr, packet.arg());
                case ALLY_ACCEPT -> GuildEvents.handleAllyAccept(player, mgr, packet.arg());
                case ALLY_DECLINE -> GuildEvents.handleAllyDecline(player, mgr, packet.arg());
                case ALLY_BREAK -> GuildEvents.handleAllyBreak(player, mgr, packet.arg());
                case GUILD_CHAT -> GuildEvents.handleGuildChat(player, mgr, packet.arg());
                case ALLY_CHAT -> GuildEvents.handleAllyChat(player, mgr, packet.arg());
                case WIKI_SET -> GuildEvents.handleWikiSet(player, mgr, packet.arg());
                case WIKI_DELETE -> GuildEvents.handleWikiDelete(player, mgr, packet.arg());
            }
        });
    }
}