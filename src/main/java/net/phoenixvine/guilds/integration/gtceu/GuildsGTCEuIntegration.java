package net.phoenixvine.guilds.integration.gtceu;

import net.minecraftforge.fml.ModList;

public final class GuildsGTCEuIntegration {

    public static final String GTCEU_MOD_ID = "gtceu";

    private GuildsGTCEuIntegration() {}

    public static boolean isAvailable() {
        return ModList.get().isLoaded(GTCEU_MOD_ID);
    }
}
