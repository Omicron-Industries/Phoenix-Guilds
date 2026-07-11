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
        // 1. Only run at the END of the tick to prevent double-firing
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();

        // 2. Heavy guard clause: ensure the game is fully loaded and active
        if (mc.player == null || mc.level == null) return;

        // 3. Typo fix (Guillds -> Guilds) and safer click consumption
        while (GuildsKeybinds.OPEN_GUILDS.consumeClick()) {
            // 4. Ensure no other screen is open before opening yours
            if (mc.screen == null) {
                mc.setScreen(new GuildScreen());
            }
        }
    }
}
