package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class C2SRequestGuildFlagPacket {

    private final UUID guildId;

    public C2SRequestGuildFlagPacket(UUID guildId) {
        this.guildId = guildId;
    }

    public C2SRequestGuildFlagPacket(FriendlyByteBuf buf) {
        this.guildId = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(guildId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            GuildManager mgr = GuildManager.get(player.getServer().overworld());
            Optional<Guild> opt = mgr.getGuildById(guildId);
            S2CGuildFlagPacket reply = opt.map(g -> new S2CGuildFlagPacket(guildId, g.getFlagIconId(),
                    g.getFlagPixelData(), g.isFlagUseDrawing(), g.getFlagWidth(), g.getFlagHeight()))

                    .orElseGet(() -> new S2CGuildFlagPacket(guildId, "", "", false, Guild.DEFAULT_FLAG_SIZE,
                            Guild.DEFAULT_FLAG_SIZE));
            GuildNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), reply);
        });
        ctx.get().setPacketHandled(true);
    }
}
