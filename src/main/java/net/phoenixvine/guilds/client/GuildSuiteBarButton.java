package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.phoenixvine.guilds.PhoenixGuilds;

@Mod.EventBusSubscriber(modid = PhoenixGuilds.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class GuildSuiteBarButton {

    private GuildSuiteBarButton() {}

    private static final String[] SUITE_ORDER = {
            "solaris", "phoenix_essentials", "phoenix_domains", "phoenix_chronicles", "phoenix_guilds",
            "phoenix_excavate"
    };

    private static final int BTN_SIZE = 20;
    private static final int GAP = 2;
    private static final int MARGIN = 4;

    private static final int GRID_COLUMNS = 3;

    private static final String SELF_ID = PhoenixGuilds.MOD_ID;

    private static final ResourceLocation ICON_TEXTURE = new ResourceLocation(PhoenixGuilds.MOD_ID,
            "textures/gui/suite_bar_icon.png");

    private static int iconCountFor(String modId) {
        if (!modId.equals("phoenix_essentials")) return 1;
        Minecraft mc = Minecraft.getInstance();
        return (mc.player != null && mc.player.hasPermissions(2)) ? 5 : 2;
    }

    private static int mySlotIndex(String selfId) {
        int idx = 0;
        for (String id : SUITE_ORDER) {
            if (id.equals(selfId)) return idx;
            if (ModList.get().isLoaded(id)) idx += iconCountFor(id);
        }
        return idx;
    }

    private static int totalLoadedIconCount() {
        int count = 0;
        for (String id : SUITE_ORDER) if (ModList.get().isLoaded(id)) count += iconCountFor(id);
        return count;
    }

    private static int btnX() {
        int col = mySlotIndex(SELF_ID) % GRID_COLUMNS;
        return MARGIN + col * (BTN_SIZE + GAP);
    }

    private static int btnY() {
        int row = mySlotIndex(SELF_ID) / GRID_COLUMNS;
        return MARGIN + row * (BTN_SIZE + GAP);
    }

    private static boolean isHovered(double mouseX, double mouseY) {
        int x = btnX();
        int y = btnY();
        return mouseX >= x && mouseX < x + BTN_SIZE && mouseY >= y && mouseY < y + BTN_SIZE;
    }

    private static boolean screenWantsBar(Screen screen) {
        if (screen instanceof SuiteHudBarAware) return true;
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;

        Inventory playerInv = mc.player.getInventory();
        for (Slot slot : containerScreen.getMenu().slots) {
            if (slot.container == playerInv) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !screenWantsBar(event.getScreen())) return;

        draw(event.getGuiGraphics(), mc, event.getMouseX(), event.getMouseY());
    }

    private static void draw(GuiGraphics gfx, Minecraft mc, double hoverMx, double hoverMy) {
        boolean hovered = isHovered(hoverMx, hoverMy);

        int x = btnX();
        int y = btnY();

        int bg = hovered ? GuildThemeUtils.C_ACCENT : GuildThemeUtils.C_PANEL;
        int border = GuildThemeUtils.C_BORDER;

        gfx.fill(x, y, x + BTN_SIZE, y + BTN_SIZE, bg);
        gfx.fill(x, y, x + BTN_SIZE, y + 1, border);
        gfx.fill(x, y + BTN_SIZE - 1, x + BTN_SIZE, y + BTN_SIZE, border);
        gfx.fill(x, y, x + 1, y + BTN_SIZE, border);
        gfx.fill(x + BTN_SIZE - 1, y, x + BTN_SIZE, y + BTN_SIZE, border);

        gfx.blit(ICON_TEXTURE, x + 2, y + 2, 0, 0, 16, 16, 16, 16);

        if (hovered) {
            gfx.renderTooltip(mc.font, Component.literal("§fOpen Guild Menu"), (int) hoverMx, (int) hoverMy);
        }
    }

    @SuppressWarnings("unused")
    private static void drawFlagGlyph(GuiGraphics gfx, int x, int y) {
        int pad = 4;
        int poleX = x + pad;
        int poleTop = y + pad - 1;
        int poleBottom = y + BTN_SIZE - pad;

        gfx.fill(poleX, poleTop, poleX + 1, poleBottom + 1, GuildThemeUtils.C_TEXT);

        int flagLeft = poleX + 1;
        int flagTop = poleTop;
        int flagW = BTN_SIZE - pad - (flagLeft - x);
        int flagH = 6;
        gfx.fill(flagLeft, flagTop, flagLeft + Math.max(flagW, 1), flagTop + flagH, GuildThemeUtils.C_GOLD);
    }

    @SubscribeEvent
    public static void onScreenMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Screen screen = event.getScreen();
        if (mc.player == null || !screenWantsBar(screen)) return;

        if (event.getButton() != 0) return;

        if (isHovered(event.getMouseX(), event.getMouseY())) {
            event.setCanceled(true);

            mc.setScreen(new GuildScreen(screen));
        }
    }
}
