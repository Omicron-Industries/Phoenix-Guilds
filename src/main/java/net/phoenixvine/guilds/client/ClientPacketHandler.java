package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;

public class ClientPacketHandler {

    public static void openGuildScreen() {
        Minecraft.getInstance().setScreen(new GuildScreen());
    }
}
