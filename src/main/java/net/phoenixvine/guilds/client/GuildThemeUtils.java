package net.phoenixvine.guilds.client;

import net.minecraft.util.Mth;
import net.phoenixvine.wiki.theme.PhoenixTheme;

public final class GuildThemeUtils {

    private GuildThemeUtils() {}

    public static int C_BG;
    public static int C_PANEL;
    public static int C_HEADER;
    public static int C_BORDER;
    public static int C_BORDER2; 
    public static int C_ACCENT;
    public static int C_ALLY;
    public static int C_TEXT;
    public static int C_DIM;
    public static int C_FAINT;

    public static final int C_ROW_ALT = 0x0AFFFFFF;
    public static final int C_ONLINE = 0xFF33EE77;
    public static final int C_OFFLINE = 0xFF444466;
    public static final int C_GOLD = 0xFFFFCC22;
    public static final int C_OFFICER = 0xFF88AAFF;

    public static void refreshCache() {
        PhoenixTheme current = PhoenixTheme.current();
        if (current == null) return;

        C_BG = current.bg.getColor();
        C_PANEL = current.panel.getColor();
        C_HEADER = current.header.getColor();
        C_BORDER = current.border.getColor();
        C_ACCENT = current.accent.getColor();
        C_ALLY = current.ally.getColor();
        C_TEXT = current.text.getColor();
        C_DIM = current.textDim.getColor();
        C_FAINT = current.textFaint.getColor();

        int a = (C_BORDER >> 24) & 0xFF;
        int r = Mth.clamp(((C_BORDER >> 16) & 0xFF) - 12, 0, 255);
        int g = Mth.clamp(((C_BORDER >> 8) & 0xFF) - 12, 0, 255);
        int b = Mth.clamp((C_BORDER & 0xFF) - 12, 0, 255);
        C_BORDER2 = (a << 24) | (r << 16) | (g << 8) | b;
    }

    static {
        refreshCache();
    }
}
