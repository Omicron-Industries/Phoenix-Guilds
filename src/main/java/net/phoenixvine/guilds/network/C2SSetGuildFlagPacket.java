package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;
import net.phoenixvine.guilds.event.GuildEvents;

import java.util.function.Supplier;

/**
 * Sets the guild's flag — either an item/block icon or a hand-painted pixel grid, plus render
 * size — in one shot. A dedicated packet rather than another {@code C2SGuildActionPacket} action,
 * since that packet's single-{@code String}-payload envelope would need fragile
 * delimiter-encoding to carry this many fields. Sent both from {@code GuildFlagEditorScreen}'s
 * Save button and from a flag block's instant-apply right-click (with {@code useDrawing=false}
 * and {@code pixelData=""} in that case — right-clicking with an item always sets icon mode).
 */
public class C2SSetGuildFlagPacket {

    private final boolean useDrawing;
    private final String iconId;
    private final String pixelData;
    private final int width;
    private final int height;

    public C2SSetGuildFlagPacket(boolean useDrawing, String iconId, String pixelData, int width, int height) {
        this.useDrawing = useDrawing;
        this.iconId = iconId;
        this.pixelData = pixelData;
        this.width = width;
        this.height = height;
    }

    public C2SSetGuildFlagPacket(FriendlyByteBuf buf) {
        this.useDrawing = buf.readBoolean();
        this.iconId = buf.readUtf(GuildNetworkLimits.ICON_ID_MAX);
        this.pixelData = buf.readUtf(Guild.FLAG_PIXEL_DATA_LENGTH);
        this.width = buf.readVarInt();
        this.height = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
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
            GuildEvents.handleSetFlag(player, mgr, useDrawing, iconId, pixelData, width, height);
        });
        ctx.get().setPacketHandled(true);
    }
}
