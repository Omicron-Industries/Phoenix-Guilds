package net.phoenixvine.guilds.integration.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.owner.RegisterOwnerTypeEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.guilds.PhoenixGuilds;


@Mod.EventBusSubscriber(modid = PhoenixGuilds.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GuildOwnerTypeRegistrar {

    private GuildOwnerTypeRegistrar() {}

    @SubscribeEvent
    public static void onRegisterOwnerType(RegisterOwnerTypeEvent event) {
        if (GTCEu.Mods.isFTBTeamsLoaded() || GTCEu.Mods.isArgonautsLoaded()) return;
        event.register(1, GuildOwner::new);
    }
}
