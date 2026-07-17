package net.phoenixvine.guilds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client asks for one guild's current flag by id — the lazy-fetch counterpart to {@code
 * S2CGuildSyncPacket.GuildSummary} no longer carrying flag data (see that record's own doc for
 * why). Sent by {@code GuildFlagBlockEntityRenderer} when it's about to render a placed flag
 * block whose guild isn't in (or has aged out of) {@code ClientGuildFlagCache}.
 */
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
                    // Guild no longer exists (disbanded since the requester's block entity was
                    // placed) — a real, blank reply rather than silence, so the client's cache
                    // stops re-requesting every render frame for something that'll never answer.
                    .orElseGet(() -> new S2CGuildFlagPacket(guildId, "", "", false, Guild.DEFAULT_FLAG_SIZE,
                            Guild.DEFAULT_FLAG_SIZE));
            GuildNetwork.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), reply);
        });
        ctx.get().setPacketHandled(true);
    }
}
