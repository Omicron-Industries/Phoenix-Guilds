package net.phoenixvine.guilds.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.guilds.PhoenixGuilds;
import net.phoenixvine.guilds.data.Guild;
import net.phoenixvine.guilds.data.GuildManager;

import java.util.*;

import static net.phoenixvine.guilds.utils.GuildPacketUtils.*;

@Mod.EventBusSubscriber(modid = PhoenixGuilds.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GuildEvents {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var mgr = GuildManager.get(Objects.requireNonNull(player.getServer()).overworld());

        mgr.getGuildFor(player.getUUID()).ifPresent(g -> {
            if (!g.getMotd().isBlank())
                player.sendSystemMessage(Component.literal("§6[Guild MOTD] §f" + g.getMotd()));
            g.addLog(player.getName().getString() + " logged in.");
            mgr.setDirty();
        });
        syncToPlayer(player, mgr);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        DamageSource src = event.getSource();
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        if (!(src.getEntity() instanceof ServerPlayer attacker)) return;
        if (victim.getUUID().equals(attacker.getUUID())) return;

        var mgr = GuildManager.get(Objects.requireNonNull(victim.getServer()).overworld());
        Optional<Guild> vGuild = mgr.getGuildFor(victim.getUUID());
        Optional<Guild> aGuild = mgr.getGuildFor(attacker.getUUID());

        if (vGuild.isPresent() && aGuild.isPresent() && vGuild.get().getId().equals(aGuild.get().getId()) &&
                !vGuild.get().isFriendlyFire()) {
            event.setCanceled(true);
        }
    }
}
