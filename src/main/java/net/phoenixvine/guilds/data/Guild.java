package net.phoenixvine.guilds.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Guild {

    public static final int MAX_MEMBERS = 30;
    private static final int MAX_LOG = 25;
    private static final int MAX_WIKI = 20;

    public record LogEntry(long timestamp, String message) {}

    private final UUID id;
    private String name;
    private UUID owner;

    private final Set<UUID> members = new LinkedHashSet<>();
    private final Map<UUID, GuildRank> memberRanks = new LinkedHashMap<>();
    private final Set<UUID> allies = new LinkedHashSet<>();
    private final Set<UUID> pendingOutgoing = new LinkedHashSet<>();
    private final Deque<LogEntry> log = new ArrayDeque<>();
    private final Map<String, String> wikiPages = new LinkedHashMap<>(); 

    private String motd = "";
    private String description = "";
    private boolean friendlyFire = false; 

    private String flagIconId = "";
    private String flagPixelData = "ffffff".repeat(MAX_FLAG_SIZE * MAX_FLAG_SIZE);
    private boolean flagUseDrawing = false;
    public static final int MIN_FLAG_SIZE = 8;
    public static final int MAX_FLAG_SIZE = 64;
    public static final int DEFAULT_FLAG_SIZE = 16;

    public static final int FLAG_PIXEL_DATA_LENGTH = MAX_FLAG_SIZE * MAX_FLAG_SIZE * 6;

    private static final int[] LEGACY_PALETTE = {
            0xFFFFFF, 0x9D9D97, 0x474F52, 0x1D1D21,
            0x835432, 0xB02E26, 0xF9801D, 0xFED83D,
            0x80C71F, 0x5E7C16, 0x169C9C, 0x3AB3DA,
            0x3C44AA, 0x8932B8, 0xC74EBD, 0xF38BAA,
    };

    private static String migratePixelDataIfLegacy(String data) {
        if (data == null || data.length() != MAX_FLAG_SIZE * MAX_FLAG_SIZE) return data;
        StringBuilder sb = new StringBuilder(FLAG_PIXEL_DATA_LENGTH);
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            int idx = Character.digit(c, 16);
            int rgb = LEGACY_PALETTE[idx < 0 || idx >= LEGACY_PALETTE.length ? 0 : idx];
            sb.append(String.format("%06x", rgb));
        }
        return sb.toString();
    }

    private int flagWidth = DEFAULT_FLAG_SIZE;
    private int flagHeight = DEFAULT_FLAG_SIZE;
    private double homeX, homeY, homeZ;
    private float homeYaw, homePitch;
    private ResourceLocation homeDimension = null;

    public Guild(UUID id, String name, UUID owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        members.add(owner);
        memberRanks.put(owner, GuildRank.OWNER);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID u) {
        if (memberRanks.containsKey(this.owner))
            memberRanks.put(this.owner, GuildRank.OFFICER);
        this.owner = u;
        memberRanks.put(u, GuildRank.OWNER);
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Map<UUID, GuildRank> getMemberRanks() {
        return memberRanks;
    }

    public Set<UUID> getAllies() {
        return allies;
    }

    public Set<UUID> getPendingOutgoing() {
        return pendingOutgoing;
    }

    public Deque<LogEntry> getLog() {
        return log;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String m) {
        this.motd = m;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean ff) {
        this.friendlyFire = ff;
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

    public void setFlag(boolean useDrawing, String iconId, String pixelData, int width, int height) {
        this.flagUseDrawing = useDrawing;
        if (iconId != null) this.flagIconId = iconId;
        if (pixelData != null && pixelData.length() == FLAG_PIXEL_DATA_LENGTH)
            this.flagPixelData = pixelData.toLowerCase();
        this.flagWidth = Math.max(MIN_FLAG_SIZE, Math.min(MAX_FLAG_SIZE, width));
        this.flagHeight = Math.max(MIN_FLAG_SIZE, Math.min(MAX_FLAG_SIZE, height));
    }

    public boolean isHomeSet() {
        return homeDimension != null;
    }

    public double getHomeX() {
        return homeX;
    }

    public double getHomeY() {
        return homeY;
    }

    public double getHomeZ() {
        return homeZ;
    }

    public float getHomeYaw() {
        return homeYaw;
    }

    public float getHomePitch() {
        return homePitch;
    }

    public ResourceLocation getHomeDimension() {
        return homeDimension;
    }

    public void setHome(ResourceLocation dim, double x, double y, double z, float yaw, float pitch) {
        this.homeDimension = dim;
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
        this.homeYaw = yaw;
        this.homePitch = pitch;
    }

    public GuildRank getRank(UUID uuid) {
        return memberRanks.getOrDefault(uuid, GuildRank.MEMBER);
    }

    public boolean hasRank(UUID uuid, GuildRank required) {
        return getRank(uuid).isAtLeast(required);
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
        memberRanks.putIfAbsent(uuid, GuildRank.MEMBER);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
        memberRanks.remove(uuid);
    }

    public boolean isAlly(UUID guildId) {
        return allies.contains(guildId);
    }

    public void addAlly(UUID guildId) {
        allies.add(guildId);
    }

    public void removeAlly(UUID guildId) {
        allies.remove(guildId);
    }

    public boolean hasPendingOutgoing(UUID guildId) {
        return pendingOutgoing.contains(guildId);
    }

    public void addPendingOutgoing(UUID guildId) {
        pendingOutgoing.add(guildId);
    }

    public void removePendingOutgoing(UUID guildId) {
        pendingOutgoing.remove(guildId);
    }

    public Map<String, String> getWikiPages() {
        return wikiPages;
    }

    public int getWikiPageCount() {
        return wikiPages.size();
    }

    public boolean isFull() {
        return members.size() >= MAX_MEMBERS;
    }

    public boolean wikiFull() {
        return wikiPages.size() >= MAX_WIKI;
    }

    public boolean setWikiPage(String title, String content) {
        if (!wikiPages.containsKey(title) && wikiFull()) return false;
        wikiPages.put(title, content);
        return true;
    }

    public boolean deleteWikiPage(String title) {
        return wikiPages.remove(title) != null;
    }

    public void addLog(String message) {
        log.addFirst(new LogEntry(System.currentTimeMillis(), message));
        while (log.size() > MAX_LOG) log.removeLast();
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putString("name", name);
        tag.putUUID("owner", owner);
        tag.putString("motd", motd);
        tag.putString("description", description);
        tag.putBoolean("friendlyFire", friendlyFire);
        tag.putString("flagIconId", flagIconId);
        tag.putString("flagPixelData", flagPixelData);
        tag.putBoolean("flagUseDrawing", flagUseDrawing);
        tag.putInt("flagWidth", flagWidth);
        tag.putInt("flagHeight", flagHeight);
        if (homeDimension != null) {
            tag.putString("homeDim", homeDimension.toString());
            tag.putDouble("homeX", homeX);
            tag.putDouble("homeY", homeY);
            tag.putDouble("homeZ", homeZ);
            tag.putFloat("homeYaw", homeYaw);
            tag.putFloat("homePitch", homePitch);
        }
        tag.put("members", uuidSet(members));
        tag.put("allies", uuidSet(allies));
        tag.put("pendingOutgoing", uuidSet(pendingOutgoing));

        ListTag rankList = new ListTag();
        for (Map.Entry<UUID, GuildRank> e : memberRanks.entrySet()) {
            CompoundTag r = new CompoundTag();
            r.putUUID("uuid", e.getKey());
            r.putString("rank", e.getValue().name());
            rankList.add(r);
        }
        tag.put("ranks", rankList);

        ListTag wikiList = new ListTag();
        for (Map.Entry<String, String> e : wikiPages.entrySet()) {
            CompoundTag w = new CompoundTag();
            w.putString("title", e.getKey());
            w.putString("content", e.getValue());
            wikiList.add(w);
        }
        tag.put("wiki", wikiList);

        ListTag logList = new ListTag();
        for (LogEntry entry : log) {
            CompoundTag l = new CompoundTag();
            l.putLong("ts", entry.timestamp());
            l.putString("msg", entry.message());
            logList.add(l);
        }
        tag.put("log", logList);

        return tag;
    }

    public static Guild deserialize(CompoundTag tag) {
        UUID id = tag.getUUID("id");
        String name = tag.getString("name");
        UUID owner = tag.getUUID("owner");
        Guild g = new Guild(id, name, owner);
        g.members.clear();
        g.memberRanks.clear();

        g.motd = tag.getString("motd");
        g.description = tag.getString("description");
        g.friendlyFire = tag.getBoolean("friendlyFire");
        if (tag.contains("flagIconId")) g.flagIconId = tag.getString("flagIconId");
        if (tag.contains("flagPixelData")) g.flagPixelData = migratePixelDataIfLegacy(tag.getString("flagPixelData"));
        if (tag.contains("flagUseDrawing")) g.flagUseDrawing = tag.getBoolean("flagUseDrawing");
        if (tag.contains("flagWidth")) g.flagWidth = tag.getInt("flagWidth");
        if (tag.contains("flagHeight")) g.flagHeight = tag.getInt("flagHeight");

        if (tag.contains("homeDim")) {
            g.homeDimension = new ResourceLocation(tag.getString("homeDim"));
            g.homeX = tag.getDouble("homeX");
            g.homeY = tag.getDouble("homeY");
            g.homeZ = tag.getDouble("homeZ");
            g.homeYaw = tag.getFloat("homeYaw");
            g.homePitch = tag.getFloat("homePitch");
        }

        readUuidSet(tag, "members", g.members);
        readUuidSet(tag, "allies", g.allies);
        readUuidSet(tag, "pendingOutgoing", g.pendingOutgoing);

        ListTag rankList = tag.getList("ranks", Tag.TAG_COMPOUND);
        for (int i = 0; i < rankList.size(); i++) {
            CompoundTag r = rankList.getCompound(i);
            try {
                UUID uuid = r.getUUID("uuid");
                GuildRank rank = GuildRank.valueOf(r.getString("rank"));
                g.memberRanks.put(uuid, rank);
            } catch (Exception ignored) {}
        }
        
        for (UUID m : g.members) g.memberRanks.putIfAbsent(m, GuildRank.MEMBER);
        g.memberRanks.put(owner, GuildRank.OWNER);

        ListTag wikiList = tag.getList("wiki", Tag.TAG_COMPOUND);
        for (int i = 0; i < wikiList.size(); i++) {
            CompoundTag w = wikiList.getCompound(i);
            g.wikiPages.put(w.getString("title"), w.getString("content"));
        }

        ListTag logList = tag.getList("log", Tag.TAG_COMPOUND);
        for (int i = 0; i < logList.size(); i++) {
            CompoundTag l = logList.getCompound(i);
            g.log.addLast(new LogEntry(l.getLong("ts"), l.getString("msg")));
        }

        return g;
    }

    private static ListTag uuidSet(Set<UUID> set) {
        ListTag list = new ListTag();
        for (UUID u : set) list.add(StringTag.valueOf(u.toString()));
        return list;
    }

    private static void readUuidSet(CompoundTag tag, String key, Set<UUID> out) {
        if (!tag.contains(key)) return;
        ListTag list = tag.getList(key, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            try {
                out.add(UUID.fromString(list.getString(i)));
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
