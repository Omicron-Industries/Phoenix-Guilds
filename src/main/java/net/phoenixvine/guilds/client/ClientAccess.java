package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.phoenixvine.guilds.client.screen.GuildFlagEditorScreen;

import java.util.UUID;

public final class ClientAccess {

    public static void openFlagEditor(UUID guildId) {
        Minecraft.getInstance().setScreen(new GuildFlagEditorScreen(null, guildId));
    }
}
