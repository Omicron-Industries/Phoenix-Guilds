package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.guilds.PhoenixGuilds;

@Mod.EventBusSubscriber(modid = PhoenixGuilds.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // GuildThemeUtils only resolved theme colors once, in a static initializer, so an animated
        // theme (e.g. rainbow) would freeze at whatever color it happened to be on class-load and
        // ordinary theme switches wouldn't apply without a client restart either - refresh it here
        // every tick so both cases stay live, matching how the other suite mods refresh their own
        // theme palettes every render frame.
        GuildThemeUtils.refreshCache();

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) return;

        while (GuildsKeybinds.OPEN_GUILDS.consumeClick()) {

            if (mc.screen == null) {
                mc.setScreen(new GuildScreen());
            }
        }
    }
}
