package net.phoenixvine.guilds.integration.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.machine.owner.RegisterOwnerTypeEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;

// Registers Guild Members as a choice for PlayerOwner so packdevs can use Phoenix Guilds for team-based systems.
public final class GuildOwnerTypeRegistrar {

    private GuildOwnerTypeRegistrar() {}

    @SubscribeEvent
    public static void onRegisterOwnerType(RegisterOwnerTypeEvent event) {
        if (GTCEu.Mods.isFTBTeamsLoaded() || GTCEu.Mods.isArgonautsLoaded()) return;
        event.register(1, GuildOwner::new);
    }
}
