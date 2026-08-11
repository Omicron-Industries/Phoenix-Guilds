package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.phoenixvine.guilds.PhoenixGuilds;
import net.phoenixvine.guilds.content.flag.GuildFlagBlockEntityRenderer;
import net.phoenixvine.guilds.content.flag.GuildFlagBlocks;
import net.phoenixvine.wiki.theme.PhoenixTheme;

public class PhoenixGuildsClient {

    public static void init(IEventBus modEventBus) {
        
        modEventBus.register(GuildsKeybinds.class);

        modEventBus.addListener(PhoenixGuildsClient::clientSetup);

        modEventBus.addListener(PhoenixGuildsClient::registerRenderers);

        PhoenixTheme.addChangeListener(GuildThemeUtils::refreshCache);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        
        PhoenixGuilds.LOGGER.info("Hey, we're on Minecraft version {}!", Minecraft.getInstance().getLaunchedVersion());

        event.enqueueWork(() -> {

            ItemBlockRenderTypes.setRenderLayer(GuildFlagBlocks.GUILD_FLAG.get(), RenderType.solid());
        });
    }

    private static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(GuildFlagBlocks.GUILD_FLAG_ENTITY.get(), GuildFlagBlockEntityRenderer::new);
    }
}
