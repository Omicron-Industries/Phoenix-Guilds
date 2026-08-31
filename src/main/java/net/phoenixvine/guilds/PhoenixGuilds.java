package net.phoenixvine.guilds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.phoenixvine.guilds.client.PhoenixGuildsClient;
import net.phoenixvine.guilds.content.flag.GuildFlagBlocks;
import net.phoenixvine.guilds.integration.gtceu.GuildOwnerTypeRegistrar;
import net.phoenixvine.guilds.integration.gtceu.GuildsGTCEuIntegration;
import net.phoenixvine.guilds.network.GuildNetwork;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PhoenixGuilds.MOD_ID)
public class PhoenixGuilds {

    public static final String MOD_ID = "phoenix_guilds";
    public static final Logger LOGGER = LogManager.getLogger();

    public PhoenixGuilds(IEventBus modEventBus) {
        
        GuildFlagBlocks.register(modEventBus);
        modEventBus.register(GuildNetwork.class);
        
        modEventBus.addListener(this::commonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            PhoenixGuildsClient.init(modEventBus);
        }

        if (GuildsGTCEuIntegration.isAvailable()) {
            try {
                modEventBus.register(GuildOwnerTypeRegistrar.class);
            } catch (Throwable t) {
                LOGGER.error("GregTech-Modern is present but its integration failed to register/ Machine " +
                        "ownership won't be guild-aware this session.", t);
            }
        }

    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Initializing Guild Network safely...");

            LOGGER.info("Hello from common setup! Found a {}!", Items.DIAMOND);
        });
    }
}
