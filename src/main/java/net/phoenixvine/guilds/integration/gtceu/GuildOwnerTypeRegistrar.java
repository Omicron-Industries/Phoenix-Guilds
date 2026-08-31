package net.phoenixvine.guilds.integration.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.owner.RegisterOwnerTypeEvent;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.phoenixvine.guilds.PhoenixGuilds;

@EventBusSubscriber(modid = PhoenixGuilds.MOD_ID)
public final class GuildOwnerTypeRegistrar {

    private GuildOwnerTypeRegistrar() {}

    @SubscribeEvent
    public static void onRegisterOwnerType(RegisterOwnerTypeEvent event) {
        if (GTCEu.Mods.isFTBTeamsLoaded() || GTCEu.Mods.isArgonautsLoaded()) return;
        event.register(1, GuildOwner::new);
    }
}