package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes; 
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.phoenixvine.guilds.PhoenixGuilds;
import net.phoenixvine.guilds.content.flag.GuildFlagBlockEntityRenderer;
import net.phoenixvine.guilds.content.flag.GuildFlagBlocks;
import net.phoenixvine.wiki.client.suite.SuiteHudBar;
import net.phoenixvine.wiki.theme.PhoenixTheme;

public class PhoenixGuildsClient {

    public static void init(IEventBus modEventBus) {
        
        modEventBus.addListener(GuildsKeybinds::register);

        modEventBus.addListener(PhoenixGuildsClient::clientSetup);

        modEventBus.addListener(PhoenixGuildsClient::registerRenderers);

        PhoenixTheme.addChangeListener(GuildThemeUtils::refreshCache);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        PhoenixGuilds.LOGGER.info("Hey, we're on Minecraft version {}!", Minecraft.getInstance().getLaunchedVersion());

        event.enqueueWork(() -> {

            ItemBlockRenderTypes.setRenderLayer(GuildFlagBlocks.GUILD_FLAG.get(), RenderType.solid());
        });

        SuiteHudBar.register(PhoenixGuilds.MOD_ID, SuiteHudBar.PRIORITY_GUILDS,
                ResourceLocation.fromNamespaceAndPath(PhoenixGuilds.MOD_ID, "textures/gui/suite_bar_icon.png"),
                Component.literal("§fOpen Guild Menu"),
                () -> Minecraft.getInstance().setScreen(
                        new GuildScreen(Minecraft.getInstance().screen)));
    }

    private static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(GuildFlagBlocks.GUILD_FLAG_ENTITY.get(), GuildFlagBlockEntityRenderer::new);
    }
}