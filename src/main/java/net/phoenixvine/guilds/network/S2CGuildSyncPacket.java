package net.phoenixvine.guilds.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.phoenixvine.guilds.data.Guild;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public record S2CGuildSyncPacket(
        String guildName,
        UUID ownerUUID,
        String motd,
        String description,
        boolean friendlyFire,
        boolean homeSet,
        String flagIconId,
        String flagPixelData,
        boolean flagUseDrawing,
        int flagWidth,
        int flagHeight,
        List<MemberEntry> members,
        List<AllyEntry> allies,
        List<PendingEntry> pendingOutgoing,
        List<PendingEntry> pendingIncoming,
        List<LogEntry> logEntries,
        List<WikiPage> wikiPages,
        List<GuildSummary> allGuilds
) implements CustomPacketPayload {

    public static final Type<S2CGuildSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("phoenixvine_guilds", "guild_sync")
    );

    public static final StreamCodec<ByteBuf, MemberEntry> MEMBER_STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MemberEntry::uuid,
            ByteBufCodecs.stringUtf8(GuildNetworkLimits.MEMBER_NAME_MAX), MemberEntry::name,
            ByteBufCodecs.BOOL, MemberEntry::isOnline,
            ByteBufCodecs.stringUtf8(GuildNetworkLimits.RANK_MAX), MemberEntry::rank,
            MemberEntry::new
    );

    public static final StreamCodec<ByteBuf, GuildSummary> GUILD_SUMMARY_STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, GuildSummary::id,
            ByteBufCodecs.stringUtf8(GuildNetworkLimits.NAME_MAX), GuildSummary::name,
            ByteBufCodecs.VAR_INT, GuildSummary::memberCount,
            ByteBufCodecs.VAR_INT, GuildSummary::onlineCount,
            ByteBufCodecs.stringUtf8(GuildNetworkLimits.DESCRIPTION_MAX), GuildSummary::description,
            GuildSummary::new
    );

    public static final StreamCodec<ByteBuf, AllyEntry> ALLY_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(GuildNetworkLimits.NAME_MAX), AllyEntry::name,
            ByteBufCodecs.VAR_INT, AllyEntry::memberCount,
            ByteBufCodecs.VAR_INT, AllyEntry::onlineCount,
            AllyEntry::new
    );

    public static final StreamCodec<ByteBuf, PendingEntry> PENDING_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(GuildNetworkLimits.NAME_MAX), PendingEntry::guildName,
            PendingEntry::new
    );

    public static final StreamCodec<ByteBuf, LogEntry> LOG_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, LogEntry::timestamp,
            ByteBufCodecs.stringUtf8(GuildNetworkLimits.LOG_MESSAGE_MAX), LogEntry::message,
            LogEntry::new
    );

    public static final StreamCodec<ByteBuf, WikiPage> WIKI_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(GuildNetworkLimits.WIKI_TITLE_MAX), WikiPage::title,
            ByteBufCodecs.stringUtf8(GuildNetworkLimits.WIKI_CONTENT_MAX), WikiPage::content,
            WikiPage::new
    );

    public static final StreamCodec<ByteBuf, S2CGuildSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                FriendlyByteBuf fbuf = new FriendlyByteBuf(buf);
                boolean inGuild = packet.guildName != null;
                fbuf.writeBoolean(inGuild);
                if (inGuild) {
                    fbuf.writeUtf(packet.guildName, GuildNetworkLimits.NAME_MAX);
                    fbuf.writeUUID(packet.ownerUUID);
                    fbuf.writeUtf(packet.motd, GuildNetworkLimits.MOTD_MAX);
                    fbuf.writeUtf(packet.description, GuildNetworkLimits.DESCRIPTION_MAX);
                    fbuf.writeBoolean(packet.friendlyFire);
                    fbuf.writeBoolean(packet.homeSet);
                    fbuf.writeUtf(packet.flagIconId, GuildNetworkLimits.ICON_ID_MAX);
                    fbuf.writeUtf(packet.flagPixelData, Guild.FLAG_PIXEL_DATA_LENGTH);
                    fbuf.writeBoolean(packet.flagUseDrawing);
                    fbuf.writeVarInt(packet.flagWidth);
                    fbuf.writeVarInt(packet.flagHeight);

                    writeList(fbuf, packet.members, MEMBER_STREAM_CODEC);
                    writeList(fbuf, packet.allies, ALLY_STREAM_CODEC);
                    writeList(fbuf, packet.pendingOutgoing, PENDING_STREAM_CODEC);
                    writeList(fbuf, packet.pendingIncoming, PENDING_STREAM_CODEC);
                    writeList(fbuf, packet.logEntries, LOG_STREAM_CODEC);
                    writeList(fbuf, packet.wikiPages, WIKI_STREAM_CODEC);
                }
                writeList(fbuf, packet.allGuilds, GUILD_SUMMARY_STREAM_CODEC);
            },
            buf -> {
                FriendlyByteBuf fbuf = new FriendlyByteBuf(buf);
                boolean inGuild = fbuf.readBoolean();
                String gName;
                UUID oUUID;
                String m;
                String desc;
                boolean fFire;
                boolean hSet;
                String fIconId;
                String fPixelData;
                boolean fUseDrawing;
                int fWidth;
                int fHeight;
                List<MemberEntry> mems;
                List<AllyEntry> aEntries;
                List<PendingEntry> pOut;
                List<PendingEntry> pIn;
                List<LogEntry> lEntries;
                List<WikiPage> wPages;

                if (inGuild) {
                    gName = fbuf.readUtf(GuildNetworkLimits.NAME_MAX);
                    oUUID = fbuf.readUUID();
                    m = fbuf.readUtf(GuildNetworkLimits.MOTD_MAX);
                    desc = fbuf.readUtf(GuildNetworkLimits.DESCRIPTION_MAX);
                    fFire = fbuf.readBoolean();
                    hSet = fbuf.readBoolean();
                    fIconId = fbuf.readUtf(GuildNetworkLimits.ICON_ID_MAX);
                    fPixelData = fbuf.readUtf(Guild.FLAG_PIXEL_DATA_LENGTH);
                    fUseDrawing = fbuf.readBoolean();
                    fWidth = fbuf.readVarInt();
                    fHeight = fbuf.readVarInt();
                    mems = readList(fbuf, MEMBER_STREAM_CODEC);
                    aEntries = readList(fbuf, ALLY_STREAM_CODEC);
                    pOut = readList(fbuf, PENDING_STREAM_CODEC);
                    pIn = readList(fbuf, PENDING_STREAM_CODEC);
                    lEntries = readList(fbuf, LOG_STREAM_CODEC);
                    wPages = readList(fbuf, WIKI_STREAM_CODEC);
                } else {
                    gName = null;
                    oUUID = null;
                    m = "";
                    desc = "";
                    fFire = false;
                    hSet = false;
                    fIconId = "";
                    fPixelData = "0".repeat(Guild.FLAG_PIXEL_DATA_LENGTH);
                    fUseDrawing = false;
                    fWidth = 16;
                    fHeight = 16;
                    mems = List.of();
                    aEntries = List.of();
                    pOut = List.of();
                    pIn = List.of();
                    lEntries = List.of();
                    wPages = List.of();
                }
                List<GuildSummary> allG = readList(fbuf, GUILD_SUMMARY_STREAM_CODEC);

                return new S2CGuildSyncPacket(
                        gName, oUUID, m, desc, fFire, hSet, fIconId, fPixelData,
                        fUseDrawing, fWidth, fHeight, mems, aEntries, pOut, pIn,
                        lEntries, wPages, allG
                );
            }
    );

    @Override
    public @NotNull Type<S2CGuildSyncPacket> type() {
        return TYPE;
    }

    public static void handle(S2CGuildSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> net.phoenixvine.guilds.client.ClientGuildCache.handleSyncPacket(packet));
    }

    private static <T> void writeList(FriendlyByteBuf buf, List<T> list, StreamCodec<ByteBuf, T> codec) {
        buf.writeVarInt(list.size());
        for (T item : list) {
            codec.encode(buf, item);
        }
    }

    private static <T> List<T> readList(FriendlyByteBuf buf, StreamCodec<ByteBuf, T> codec) {
        int size = buf.readVarInt();
        List<T> list = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(codec.decode(buf));
        }
        return list;
    }

    public record MemberEntry(UUID uuid, String name, boolean isOnline, String rank) {}
    public record GuildSummary(UUID id, String name, int memberCount, int onlineCount, String description) {}
    public record AllyEntry(String name, int memberCount, int onlineCount) {}
    public record PendingEntry(String guildName) {}
    public record LogEntry(long timestamp, String message) {}
    public record WikiPage(String title, String content) {}
}