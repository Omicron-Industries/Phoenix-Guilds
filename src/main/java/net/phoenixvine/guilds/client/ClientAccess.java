package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;

/**
 * Safely handles client-only methods.
 * This class is never loaded on the dedicated server.
 */
public final class ClientAccess {

    public static void openFlagEditor() {
        Minecraft.getInstance().setScreen(new GuildFlagEditorScreen(null));
    }
}
