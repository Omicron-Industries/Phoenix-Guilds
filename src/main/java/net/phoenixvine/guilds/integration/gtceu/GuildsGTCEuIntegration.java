package net.phoenixvine.guilds.integration.gtceu;

import net.minecraftforge.fml.ModList;

/**
 * Optional integration with GregTech-Modern (gtceu) — {@code gtceu} is a non-mandatory
 * {@code mods.toml} dependency, so this class must contain ONLY the availability check, zero
 * imports or references to anything in {@code com.gregtechceu.*}. Merely calling a static method
 * on a class forces the JVM to verify that class's whole bytecode, so if the real
 * GTCEu-referencing logic ({@link GuildOwner}, {@link GuildOwnerTypeRegistrar}) lived here,
 * calling {@link #isAvailable()} on a system without GTCEu installed would already try (and fail)
 * to resolve GTCEu's types. Same two-class-split reasoning as Phoenix-Domains'
 * {@code DomainsChroniclesIntegration}/{@code ChroniclesQuestFlagRegistrar}.
 */
public final class GuildsGTCEuIntegration {

    public static final String GTCEU_MOD_ID = "gtceu";

    private GuildsGTCEuIntegration() {}

    public static boolean isAvailable() {
        return ModList.get().isLoaded(GTCEU_MOD_ID);
    }
}
