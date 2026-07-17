package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.phoenixvine.guilds.PhoenixGuilds;
import net.phoenixvine.guilds.content.flag.GuildFlagBlockEntityRenderer;
import net.phoenixvine.guilds.content.flag.GuildFlagBlocks;

public class PhoenixGuildsClient {

    public static void init(IEventBus modEventBus) {
        // Register keybinds on the mod bus safely here
        modEventBus.register(GuildsKeybinds.class);

        // Listen to client setup safely here
        modEventBus.addListener(PhoenixGuildsClient::clientSetup);

        modEventBus.addListener(PhoenixGuildsClient::registerRenderers);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        // Safe to use Minecraft.getInstance() here now!
        PhoenixGuilds.LOGGER.info("Hey, we're on Minecraft version {}!", Minecraft.getInstance().getLaunchedVersion());
    }

    private static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(GuildFlagBlocks.GUILD_FLAG_ENTITY.get(), GuildFlagBlockEntityRenderer::new);
    }
}
