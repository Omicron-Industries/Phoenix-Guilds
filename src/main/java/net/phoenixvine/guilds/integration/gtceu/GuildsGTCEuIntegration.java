package net.phoenixvine.guilds.integration.gtceu;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraftforge.fml.ModList;
import net.phoenixvine.guilds.PhoenixGuilds;

import java.util.UUID;

public final class GuildsGTCEuIntegration {

    private GuildsGTCEuIntegration() {}

    public static boolean isAvailable() {
        return ModList.get().isLoaded(GTCEu.MOD_ID);
    }

    public static void migrateGTCEuOwnership(UUID from, UUID to) {
        if (!GuildsGTCEuIntegration.isAvailable()) return;
        try {
            GTCEuVirtualRegistrySync.migrateToGuild(from, to);
        } catch (Throwable t) {
            PhoenixGuilds.LOGGER.error("GregTech-Modern is present but migrating virtual registry ownership" +
                    " failed. Any existing Ender Link Cover networks may need manual reconfiguring.", t);
        }
    }
}
