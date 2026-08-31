package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.phoenixvine.guilds.PhoenixGuilds;

@EventBusSubscriber(modid = PhoenixGuilds.MOD_ID, value = Dist.CLIENT)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

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