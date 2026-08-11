package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.phoenixvine.guilds.data.Guild;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class S2CGuildSyncPacket {

    public record MemberEntry(UUID uuid, String name, boolean isOnline, String rank) {}

    public record GuildSummary(UUID id, String name, int memberCount, int onlineCount, String description) {}

    public record AllyEntry(String name, int memberCount, int onlineCount) {}

    public record PendingEntry(String guildName) {}

    public record LogEntry(long timestamp, String message) {}

    public record WikiPage(String title, String content) {}

    private final String guildName;
    private final UUID ownerUUID;
    private final String motd;
    private final String description;
    private final boolean friendlyFire;
    private final boolean homeSet;
    private final String flagIconId;
    private final String flagPixelData;
    private final boolean flagUseDrawing;
    private final int flagWidth;
    private final int flagHeight;
    private final List<MemberEntry> members;
    private final List<AllyEntry> allies;
    private final List<PendingEntry> pendingOutgoing;
    private final List<PendingEntry> pendingIncoming;
    private final List<LogEntry> logEntries;
    private final List<WikiPage> wikiPages;
    private final List<GuildSummary> allGuilds;

    public S2CGuildSyncPacket(String guildName, UUID ownerUUID, String motd, String description,
                              boolean friendlyFire, boolean homeSet, String flagIconId, String flagPixelData,
                              boolean flagUseDrawing, int flagWidth, int flagHeight, List<MemberEntry> members,
                              List<AllyEntry> allies, List<PendingEntry> pendingOutgoing,
                              List<PendingEntry> pendingIncoming, List<LogEntry> logEntries,
                              List<WikiPage> wikiPages, List<GuildSummary> allGuilds) {
        this.guildName = guildName;
        this.ownerUUID = ownerUUID;
        this.motd = motd;
        this.description = description;
        this.friendlyFire = friendlyFire;
        this.homeSet = homeSet;
        this.flagIconId = flagIconId;
        this.flagPixelData = flagPixelData;
        this.flagUseDrawing = flagUseDrawing;
        this.flagWidth = flagWidth;
        this.flagHeight = flagHeight;
        this.members = members;
        this.allies = allies;
        this.pendingOutgoing = pendingOutgoing;
        this.pendingIncoming = pendingIncoming;
        this.logEntries = logEntries;
        this.wikiPages = wikiPages;
        this.allGuilds = allGuilds;
    }

    public S2CGuildSyncPacket(FriendlyByteBuf buf) {
        boolean inGuild = buf.readBoolean();
        if (inGuild) {
            this.guildName = buf.readUtf(GuildNetworkLimits.NAME_MAX);
            this.ownerUUID = buf.readUUID();
            this.motd = buf.readUtf(GuildNetworkLimits.MOTD_MAX);
            this.description = buf.readUtf(GuildNetworkLimits.DESCRIPTION_MAX);
            this.friendlyFire = buf.readBoolean();
            this.homeSet = buf.readBoolean();
            this.flagIconId = buf.readUtf(GuildNetworkLimits.ICON_ID_MAX);
            this.flagPixelData = buf.readUtf(Guild.FLAG_PIXEL_DATA_LENGTH);
            this.flagUseDrawing = buf.readBoolean();
            this.flagWidth = buf.readVarInt();
            this.flagHeight = buf.readVarInt();
            this.members = readList(buf, b -> new MemberEntry(b.readUUID(),
                    b.readUtf(GuildNetworkLimits.MEMBER_NAME_MAX), b.readBoolean(),
                    b.readUtf(GuildNetworkLimits.RANK_MAX)));
            this.allies = readList(buf, b -> new AllyEntry(b.readUtf(GuildNetworkLimits.NAME_MAX), b.readVarInt(),
                    b.readVarInt()));
            this.pendingOutgoing = readList(buf, b -> new PendingEntry(b.readUtf(GuildNetworkLimits.NAME_MAX)));
            this.pendingIncoming = readList(buf, b -> new PendingEntry(b.readUtf(GuildNetworkLimits.NAME_MAX)));
            this.logEntries = readList(buf,
                    b -> new LogEntry(b.readLong(), b.readUtf(GuildNetworkLimits.LOG_MESSAGE_MAX)));
            this.wikiPages = readList(buf, b -> new WikiPage(b.readUtf(GuildNetworkLimits.WIKI_TITLE_MAX),
                    b.readUtf(GuildNetworkLimits.WIKI_CONTENT_MAX)));
        } else {
            this.guildName = null;
            this.ownerUUID = null;
            this.motd = "";
            this.description = "";
            this.friendlyFire = false;
            this.homeSet = false;
            this.flagIconId = "";
            this.flagPixelData = "0".repeat(Guild.FLAG_PIXEL_DATA_LENGTH);
            this.flagUseDrawing = false;
            this.flagWidth = 16;
            this.flagHeight = 16;
            this.members = List.of();
            this.allies = List.of();
            this.pendingOutgoing = List.of();
            this.pendingIncoming = List.of();
            this.logEntries = List.of();
            this.wikiPages = List.of();
        }
        this.allGuilds = readList(buf,
                b -> new GuildSummary(b.readUUID(), b.readUtf(GuildNetworkLimits.NAME_MAX), b.readVarInt(),
                        b.readVarInt(), b.readUtf(GuildNetworkLimits.DESCRIPTION_MAX)));
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

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> net.phoenixvine.guilds.client.ClientGuildCache.handleSyncPacket(this)));
        ctx.get().setPacketHandled(true);
    }

    public String getGuildName() {
        return guildName;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getMotd() {
        return motd;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public boolean isHomeSet() {
        return homeSet;
    }

    public String getFlagIconId() {
        return flagIconId;
    }

    public String getFlagPixelData() {
        return flagPixelData;
    }

    public boolean isFlagUseDrawing() {
        return flagUseDrawing;
    }

    public int getFlagWidth() {
        return flagWidth;
    }

    public int getFlagHeight() {
        return flagHeight;
    }

    public List<MemberEntry> getMembers() {
        return members;
    }

    public List<AllyEntry> getAllies() {
        return allies;
    }

    public List<PendingEntry> getPendingOutgoing() {
        return pendingOutgoing;
    }

    public List<PendingEntry> getPendingIncoming() {
        return pendingIncoming;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public List<WikiPage> getWikiPages() {
        return wikiPages;
    }

    public List<GuildSummary> getAllGuilds() {
        return allGuilds;
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
