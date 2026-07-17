package net.phoenixvine.guilds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    public PhoenixGuilds() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        GuildFlagBlocks.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        // Safely initialize the client side ONLY if running on a physical client
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PhoenixGuildsClient.init(modEventBus));

        // Registration happens once here, not per-tick, so there's nothing to retry if it fails —
        // just log and move on, same as every other optional integration in this mod family
        // degrading instead of crashing when a soft dependency's jar is present but broken.
        if (GuildsGTCEuIntegration.isAvailable()) {
            try {
                modEventBus.register(GuildOwnerTypeRegistrar.class);
            } catch (Throwable t) {
                LOGGER.error("GregTech-Modern is present but its integration failed to register — machine" +
                        " ownership won't be guild-aware this session.", t);
            }
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Initializing Guild Network safely...");
            GuildNetwork.init();

            LOGGER.info("Hello from common setup! Found a {}!", Items.DIAMOND);
        });
    }
}
