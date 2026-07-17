package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.phoenixvine.guilds.data.Guild;

import java.util.UUID;

/**
 * Server's targeted reply to {@link C2SRequestGuildFlagPacket} — one guild's current flag,
 * sent only to the player who asked, never broadcast.
 */
public class S2CGuildFlagPacket {

    private final UUID guildId;
    private final String flagIconId;
    private final String flagPixelData;
    private final boolean flagUseDrawing;
    private final int flagWidth;
    private final int flagHeight;

    public S2CGuildFlagPacket(UUID guildId, String flagIconId, String flagPixelData, boolean flagUseDrawing,
                              int flagWidth, int flagHeight) {
        this.guildId = guildId;
        this.flagIconId = flagIconId;
        this.flagPixelData = flagPixelData;
        this.flagUseDrawing = flagUseDrawing;
        this.flagWidth = flagWidth;
        this.flagHeight = flagHeight;
    }

    public S2CGuildFlagPacket(FriendlyByteBuf buf) {
        this.guildId = buf.readUUID();
        this.flagIconId = buf.readUtf(GuildNetworkLimits.ICON_ID_MAX);
        this.flagPixelData = buf.readUtf(Guild.FLAG_PIXEL_DATA_LENGTH);
        this.flagUseDrawing = buf.readBoolean();
        this.flagWidth = buf.readVarInt();
        this.flagHeight = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(guildId);
        buf.writeUtf(flagIconId, GuildNetworkLimits.ICON_ID_MAX);
        buf.writeUtf(flagPixelData, Guild.FLAG_PIXEL_DATA_LENGTH);
        buf.writeBoolean(flagUseDrawing);
        buf.writeVarInt(flagWidth);
        buf.writeVarInt(flagHeight);
    }

    // Getters so the network handler can safely read the data
    public UUID getGuildId() {
        return guildId;
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
}
