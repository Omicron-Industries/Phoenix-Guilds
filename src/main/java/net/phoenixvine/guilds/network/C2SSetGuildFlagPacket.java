package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;
import net.phoenixvine.guilds.event.GuildEvents;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class C2SSetGuildFlagPacket {

    @Nullable
    private final UUID targetGuildId;
    private final boolean useDrawing;
    private final String iconId;
    private final String pixelData;
    private final int width;
    private final int height;

    public C2SSetGuildFlagPacket(@Nullable UUID targetGuildId, boolean useDrawing, String iconId, String pixelData,
                                 int width, int height) {
        this.targetGuildId = targetGuildId;
        this.useDrawing = useDrawing;
        this.iconId = iconId;
        this.pixelData = pixelData;
        this.width = width;
        this.height = height;
    }

    public C2SSetGuildFlagPacket(FriendlyByteBuf buf) {
        this.targetGuildId = buf.readBoolean() ? buf.readUUID() : null;
        this.useDrawing = buf.readBoolean();
        this.iconId = buf.readUtf(GuildNetworkLimits.ICON_ID_MAX);
        this.pixelData = buf.readUtf(Guild.FLAG_PIXEL_DATA_LENGTH);
        this.width = buf.readVarInt();
        this.height = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(targetGuildId != null);
        if (targetGuildId != null) buf.writeUUID(targetGuildId);
        buf.writeBoolean(useDrawing);
        buf.writeUtf(iconId, GuildNetworkLimits.ICON_ID_MAX);
        buf.writeUtf(pixelData, Guild.FLAG_PIXEL_DATA_LENGTH);
        buf.writeVarInt(width);
        buf.writeVarInt(height);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            GuildManager mgr = GuildManager.get(player.getServer().overworld());
            GuildEvents.handleSetFlag(player, mgr, targetGuildId, useDrawing, iconId, pixelData, width, height);
        });
        ctx.get().setPacketHandled(true);
    }
}
