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

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.level == null) return;

        while (GuildsKeybinds.OPEN_GUILDS.consumeClick()) {
            
            if (mc.screen == null) {
                mc.setScreen(new GuildScreen());
            }
        }
    }
}
