package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.phoenixvine.guilds.data.Guild;

import java.util.ArrayList;
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
                                 List<GuildSummary> allGuilds) {

    public record MemberEntry(UUID uuid, String name, boolean isOnline, String rank) {}

    public record GuildSummary(UUID id, String name, int memberCount, int onlineCount, String description) {}

    public record AllyEntry(String name, int memberCount, int onlineCount) {}

    public record PendingEntry(String guildName) {}

    public record LogEntry(long timestamp, String message) {}

    public record WikiPage(String title, String content) {}

    public static S2CGuildSyncPacket decode(FriendlyByteBuf buf) {
        boolean inGuild = buf.readBoolean();
        String guildName;
        UUID ownerUUID;
        String motd;
        String description;
        boolean friendlyFire;
        boolean homeSet;
        String flagIconId;
        String flagPixelData;
        boolean flagUseDrawing;
        int flagWidth;
        int flagHeight;
        List<MemberEntry> members;
        List<AllyEntry> allies;
        List<PendingEntry> pendingOutgoing;
        List<PendingEntry> pendingIncoming;
        List<LogEntry> logEntries;
        List<WikiPage> wikiPages;

        if (inGuild) {
            guildName = buf.readUtf(GuildNetworkLimits.NAME_MAX);
            ownerUUID = buf.readUUID();
            motd = buf.readUtf(GuildNetworkLimits.MOTD_MAX);
            description = buf.readUtf(GuildNetworkLimits.DESCRIPTION_MAX);
            friendlyFire = buf.readBoolean();
            homeSet = buf.readBoolean();
            flagIconId = buf.readUtf(GuildNetworkLimits.ICON_ID_MAX);
            flagPixelData = buf.readUtf(Guild.FLAG_PIXEL_DATA_LENGTH);
            flagUseDrawing = buf.readBoolean();
            flagWidth = buf.readVarInt();
            flagHeight = buf.readVarInt();
            members = readList(buf, b -> new MemberEntry(b.readUUID(),
                    b.readUtf(GuildNetworkLimits.MEMBER_NAME_MAX), b.readBoolean(),
                    b.readUtf(GuildNetworkLimits.RANK_MAX)));
            allies = readList(buf, b -> new AllyEntry(b.readUtf(GuildNetworkLimits.NAME_MAX), b.readVarInt(),
                    b.readVarInt()));
            pendingOutgoing = readList(buf, b -> new PendingEntry(b.readUtf(GuildNetworkLimits.NAME_MAX)));
            pendingIncoming = readList(buf, b -> new PendingEntry(b.readUtf(GuildNetworkLimits.NAME_MAX)));
            logEntries = readList(buf,
                    b -> new LogEntry(b.readLong(), b.readUtf(GuildNetworkLimits.LOG_MESSAGE_MAX)));
            wikiPages = readList(buf, b -> new WikiPage(b.readUtf(GuildNetworkLimits.WIKI_TITLE_MAX),
                    b.readUtf(GuildNetworkLimits.WIKI_CONTENT_MAX)));
        } else {
            guildName = null;
            ownerUUID = null;
            motd = "";
            description = "";
            friendlyFire = false;
            homeSet = false;
            flagIconId = "";
            flagPixelData = "0".repeat(Guild.FLAG_PIXEL_DATA_LENGTH);
            flagUseDrawing = false;
            flagWidth = 16;
            flagHeight = 16;
            members = List.of();
            allies = List.of();
            pendingOutgoing = List.of();
            pendingIncoming = List.of();
            logEntries = List.of();
            wikiPages = List.of();
        }

        List<GuildSummary> allGuilds = readList(buf,
                b -> new GuildSummary(b.readUUID(), b.readUtf(GuildNetworkLimits.NAME_MAX), b.readVarInt(),
                        b.readVarInt(), b.readUtf(GuildNetworkLimits.DESCRIPTION_MAX)));

        return new S2CGuildSyncPacket(guildName, ownerUUID, motd, description, friendlyFire, homeSet,
                flagIconId, flagPixelData, flagUseDrawing, flagWidth, flagHeight,
                members, allies, pendingOutgoing, pendingIncoming, logEntries, wikiPages, allGuilds);
    }

    public void encode(FriendlyByteBuf buf) {
        boolean inGuild = guildName != null;
        buf.writeBoolean(inGuild);
        if (inGuild) {
            buf.writeUtf(guildName, GuildNetworkLimits.NAME_MAX);
            buf.writeUUID(ownerUUID);
            buf.writeUtf(motd, GuildNetworkLimits.MOTD_MAX);
            buf.writeUtf(description, GuildNetworkLimits.DESCRIPTION_MAX);
            buf.writeBoolean(friendlyFire);
            buf.writeBoolean(homeSet);
            buf.writeUtf(flagIconId, GuildNetworkLimits.ICON_ID_MAX);
            buf.writeUtf(flagPixelData, Guild.FLAG_PIXEL_DATA_LENGTH);
            buf.writeBoolean(flagUseDrawing);
            buf.writeVarInt(flagWidth);
            buf.writeVarInt(flagHeight);
            writeList(buf, members, (b, m) -> {
                b.writeUUID(m.uuid());
                b.writeUtf(m.name(), GuildNetworkLimits.MEMBER_NAME_MAX);
                b.writeBoolean(m.isOnline());
                b.writeUtf(m.rank(), GuildNetworkLimits.RANK_MAX);
            });
            writeList(buf, allies, (b, a) -> {
                b.writeUtf(a.name(), GuildNetworkLimits.NAME_MAX);
                b.writeVarInt(a.memberCount());
                b.writeVarInt(a.onlineCount());
            });
            writeList(buf, pendingOutgoing, (b, p) -> b.writeUtf(p.guildName(), GuildNetworkLimits.NAME_MAX));
            writeList(buf, pendingIncoming, (b, p) -> b.writeUtf(p.guildName(), GuildNetworkLimits.NAME_MAX));
            writeList(buf, logEntries, (b, l) -> {
                b.writeLong(l.timestamp());
                b.writeUtf(l.message(), GuildNetworkLimits.LOG_MESSAGE_MAX);
            });
            writeList(buf, wikiPages, (b, w) -> {
                b.writeUtf(w.title(), GuildNetworkLimits.WIKI_TITLE_MAX);
                b.writeUtf(w.content(), GuildNetworkLimits.WIKI_CONTENT_MAX);
            });
        }
        writeList(buf, allGuilds, (b, g) -> {
            b.writeUUID(g.id());
            b.writeUtf(g.name(), GuildNetworkLimits.NAME_MAX);
            b.writeVarInt(g.memberCount());
            b.writeVarInt(g.onlineCount());
            b.writeUtf(g.description(), GuildNetworkLimits.DESCRIPTION_MAX);
        });
    }

    @FunctionalInterface
    private interface ElemReader<T> {

        T read(FriendlyByteBuf b);
    }

    @FunctionalInterface
    private interface ElemWriter<T> {

        void write(FriendlyByteBuf b, T t);
    }

    private static <T> List<T> readList(FriendlyByteBuf buf, ElemReader<T> r) {
        int n = buf.readVarInt();
        List<T> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) out.add(r.read(buf));
        return out;
    }

    private static <T> void writeList(FriendlyByteBuf buf, List<T> list, ElemWriter<T> w) {
        buf.writeVarInt(list.size());
        for (T t : list) w.write(buf, t);
    }
}
