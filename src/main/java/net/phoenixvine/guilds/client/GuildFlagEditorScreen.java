package net.phoenixvine.guilds.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.phoenixvine.guilds.network.C2SGuildActionPacket;
import net.phoenixvine.guilds.network.GuildNetwork;

import static net.phoenixvine.guilds.client.GuildThemeUtils.*;

@OnlyIn(Dist.CLIENT)
public class GuildFlagEditorScreen extends Screen {

    private static final int COLS = 16;
    private static final int ROWS = 8;
    private int CELL;
    private int GRID_W;
    private int GRID_H;

    static final int[] PALETTE = {
            0xFFFFFFFF, 0xFF9D9D97, 0xFF474F52, 0xFF1D1D21,
            0xFF835432, 0xFFB02E26, 0xFFF9801D, 0xFFFED83D,
            0xFF80C71F, 0xFF5E7C16, 0xFF169C9C, 0xFF3AB3DA,
            0xFF3C44AA, 0xFF8932B8, 0xFFC74EBD, 0xFFF38BAA,
    };

    private final Screen parent;
    private final char[] pixels = new char[COLS * ROWS];
    private int selectedColor = 5;

    private static final int PALETTE_COLS = 8;
    private int PALETTE_CELL;
    private int PALETTE_W;

    private int PANEL_W;
    private static final int PALETTE_ROWS = 2;
    private int PANEL_H;

    private int px, py;
    private int gridX, gridY;
    private int paletteX, paletteY;

    public GuildFlagEditorScreen(Screen parent) {
        super(Component.literal("Guild Flag Editor"));
        this.parent = parent;

        String data = ClientGuildCache.flagData;
        for (int i = 0; i < COLS * ROWS; i++) {
            char c = (i < data.length()) ? data.charAt(i) : '0';
            pixels[i] = Character.isDigit(c) || (c >= 'a' && c <= 'f') ? c : '0';
        }
    }

    @Override
    protected void init() {
        CELL = Mth.clamp(Math.min((width - 40) / COLS, (height - 100) / ROWS), 8, 20);
        PALETTE_CELL = CELL + 2;
        GRID_W = COLS * CELL;
        GRID_H = ROWS * CELL;
        PALETTE_W = PALETTE_COLS * PALETTE_CELL;
        PANEL_W = GRID_W + 24;
        PANEL_H = 30 + GRID_H + 10 + PALETTE_ROWS * PALETTE_CELL + 10 + 20 + 12;

        px = (width - PANEL_W) / 2;
        py = (height - PANEL_H) / 2;
        gridX = px + (PANEL_W - GRID_W) / 2;
        gridY = py + 30;
        paletteX = px + (PANEL_W - PALETTE_W) / 2;
        paletteY = gridY + GRID_H + 10;

        int btnY = paletteY + PALETTE_ROWS * PALETTE_CELL + 10;
        int btnW = 60;

        addRenderableWidget(Button.builder(Component.literal("Save"), b -> save())
                .bounds(px + PANEL_W / 2 - btnW - 4, btnY, btnW, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> {
            assert minecraft != null;
            minecraft.setScreen(parent);
        })
                .bounds(px + PANEL_W / 2 + 4, btnY, btnW, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Clear"), b -> clearCanvas())
                .bounds(px + 4, btnY, 50, 18).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, C_BG);

        // Background Panels (Clean Variable Callouts)
        g.fill(px, py, px + PANEL_W, py + PANEL_H, C_PANEL);
        g.fill(px, py, px + PANEL_W, py + 1, C_BORDER);
        g.fill(px, py + PANEL_H - 1, px + PANEL_W, py + PANEL_H, C_BORDER);
        g.fill(px, py, px + 1, py + PANEL_H, C_BORDER);
        g.fill(px + PANEL_W - 1, py, px + PANEL_W, py + PANEL_H, C_BORDER);
        g.drawCenteredString(font, "Guild Flag Editor", px + PANEL_W / 2, py + 10, C_GOLD);

        // Grid Render Matrix
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = row * COLS + col;
                int color = PALETTE[hexVal(pixels[idx])];
                int cx = gridX + col * CELL;
                int cy = gridY + row * CELL;
                g.fill(cx, cy, cx + CELL, cy + CELL, color);

                // Fine grid alignment lines
                g.fill(cx, cy, cx + CELL, cy + 1, 0x33000000);
                g.fill(cx, cy, cx + 1, cy + CELL, 0x33000000);
            }
        }
        g.fill(gridX + GRID_W, gridY, gridX + GRID_W + 1, gridY + GRID_H, 0x33000000);
        g.fill(gridX, gridY + GRID_H, gridX + GRID_W + 1, gridY + GRID_H + 1, 0x33000000);

        // Palette Array
        for (int i = 0; i < 16; i++) {
            int col = i % PALETTE_COLS;
            int row = i / PALETTE_COLS;
            int cx = paletteX + col * PALETTE_CELL;
            int cy = paletteY + row * PALETTE_CELL;
            g.fill(cx, cy, cx + PALETTE_CELL, cy + PALETTE_CELL, PALETTE[i]);

            if (i == selectedColor) {
                g.fill(cx, cy, cx + PALETTE_CELL, cy + 1, 0xFFFFFFFF);
                g.fill(cx, cy + PALETTE_CELL - 1, cx + PALETTE_CELL, cy + PALETTE_CELL, 0xFFFFFFFF);
                g.fill(cx, cy, cx + 1, cy + PALETTE_CELL, 0xFFFFFFFF);
                g.fill(cx + PALETTE_CELL - 1, cy, cx + PALETTE_CELL, cy + PALETTE_CELL, 0xFFFFFFFF);
            }
        }

        g.drawString(font, "Selected: ", paletteX + PALETTE_W + 4, paletteY + 2, C_DIM, false);
        g.fill(paletteX + PALETTE_W + 4, paletteY + 12, paletteX + PALETTE_W + 20, paletteY + 28,
                PALETTE[selectedColor]);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // Palette Processing Click
        for (int i = 0; i < 16; i++) {
            int col = i % PALETTE_COLS;
            int row = i / PALETTE_COLS;
            int cx = paletteX + col * PALETTE_CELL;
            int cy = paletteY + row * PALETTE_CELL;
            if (mx >= cx && mx < cx + PALETTE_CELL && my >= cy && my < cy + PALETTE_CELL) {
                selectedColor = i;
                return true;
            }
        }

        // Initial Grid Press Handling
        if (mx >= gridX && mx < gridX + GRID_W && my >= gridY && my < gridY + GRID_H) {
            drawPixelAt(mx, my, btn == 1);
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        // Intercept drags directly using the active click context parameters
        if (mx >= gridX && mx < gridX + GRID_W && my >= gridY && my < gridY + GRID_H) {
            drawPixelAt(mx, my, btn == 1);
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    private void drawPixelAt(double mx, double my, boolean isErasing) {
        int col = (int) (mx - gridX) / CELL;
        int row = (int) (my - gridY) / CELL;
        if (col >= 0 && col < COLS && row >= 0 && row < ROWS) {
            int idx = row * COLS + col;
            pixels[idx] = isErasing ? '0' : Character.forDigit(selectedColor, 16);
        }
    }

    private void clearCanvas() {
        java.util.Arrays.fill(pixels, '0');
    }

    private void save() {
        String data = new String(pixels);
        GuildNetwork.CHANNEL.sendToServer(new C2SGuildActionPacket(C2SGuildActionPacket.Action.SET_FLAG, data));
        assert minecraft != null;
        minecraft.setScreen(parent);
    }

    private static int hexVal(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        return 0;
    }

    public static void renderFlag(GuiGraphics g, String flagData, int x, int y, int cellSize) {
        if (flagData == null || flagData.length() < COLS * ROWS) return;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = row * COLS + col;
                char c = flagData.charAt(idx);
                int colorIdx = hexVal(c);
                int cx = x + col * cellSize;
                int cy = y + row * cellSize;
                g.fill(cx, cy, cx + cellSize, cy + cellSize, PALETTE[colorIdx]);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
