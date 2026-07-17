package net.phoenixvine.guilds.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.phoenixvine.guilds.network.C2SSetGuildFlagPacket;
import net.phoenixvine.guilds.network.GuildNetwork;

import java.util.Arrays;

import static net.phoenixvine.guilds.client.GuildThemeUtils.C_ACCENT;
import static net.phoenixvine.guilds.client.GuildThemeUtils.C_BG;
import static net.phoenixvine.guilds.client.GuildThemeUtils.C_BORDER;
import static net.phoenixvine.guilds.client.GuildThemeUtils.C_DIM;
import static net.phoenixvine.guilds.client.GuildThemeUtils.C_PANEL;

/**
 * Edits the guild's flag — either an item/block icon or a hand-painted pixel grid, plus a render
 * size, toggled with a "Mode" button rather than one replacing the other. A live preview shows
 * whichever mode is active at the currently-configured resolution, with a "Resolution: WxH"
 * readout next to it.
 * <p>
 * Layout is two columns, not one tall stack: a left "canvas" column holds the drawing grid (or
 * icon preview) plus the palette underneath, and a right "controls" column holds the Mode
 * toggle, Size button, resolution label, Clear/Choose-Item button, and Save/Cancel. A single
 * column kept running out of vertical room well before it ran out of horizontal room — a 64x64
 * grid plus a full stack of buttons underneath it does not fit in typical window heights, and
 * clamping panelH after the fact just meant Save/Cancel silently fell below the actual screen
 * bottom (the exact "missing entirely at 32x56" failure the fixed-size and preset-resolution work
 * upstream of this was trying to eliminate). Putting the button stack beside the grid instead of
 * below it means the panel's height tracks whichever column is taller, not the sum of both.
 * <p>
 * Every control that changes the layout (mode toggle, size preset button — the drawing grid's
 * cell count tracks resolution now, see {@link GuildFlagPixelArt}) calls {@link #rebuildWidgets()}
 * rather than this class's own {@link #init()} directly: {@code Screen.init()} alone never clears
 * the existing widget list (only the outer {@code Screen.init(Minecraft, int, int)}/{@code
 * rebuildWidgets()} do, verified against decompiled {@code Screen.java}), so calling {@code init()}
 * bare left stale buttons stacked underneath the freshly-added ones.
 * <p>
 * Flag resolution is fixed-preset, not free-form: the flag is always square and picked from
 * {@link #FLAG_SIZES} via a single "Size" button that cycles through them, rather than separate
 * width/height +/- steppers that could walk the flag out to an arbitrary size.
 * <p>
 * Deliberately fixed pixel sizes, not scaled proportionally to the window/GUI-scale — an earlier
 * pass tried {@code Mth.clamp(width * fraction, min, max)}-style proportional sizing (the same
 * pattern {@code GuildScreen}'s own panel uses) and it repeatedly produced panels landing
 * partially offscreen at particular window-size/GUI-Scale/flag-resolution combinations, each fix
 * uncovering another one. Fixed sizes are simpler to reason about and can't drift out of budget
 * the same way — the only screen-size-awareness left is the defensive {@code Math.min}/{@code
 * Math.max} clamps against {@code width}/{@code height} themselves (never grow past the actual
 * window, never position with a negative origin), not an attempt to grow the panel to fill extra
 * space on a larger screen.
 */
@OnlyIn(Dist.CLIENT)
public class GuildFlagEditorScreen extends Screen {

    private enum Mode {
        ICON,
        DRAWING
    }

    /** Square-only presets. The Size button cycles through these in order. */
    private static final int[] FLAG_SIZES = { 16, 32, 64 };

    private static final int PAD = 18;
    private static final int COL_GAP = 16;
    private static final int ROW_H = 22;
    private static final int ROW_GAP = 10;
    private static final int TITLE_H = 16;
    private static final int RIGHT_COL_W = 150;
    private static final int PREVIEW_SIZE = 130;
    // The drawing grid's larger dimension (whichever of cols/rows is bigger) never renders past
    // this many pixels. With FLAG_SIZES capped at 64 this is now a belt-and-suspenders bound
    // rather than the primary defense — the preset list is what keeps the grid small — but it's
    // cheap insurance if MAX_DIM or FLAG_SIZES ever changes later.
    private static final int GRID_MAX = 200;

    private static final int PALETTE_COLS = 8;
    private static final int PALETTE_ROWS = 2;

    private int cell;
    private int cols, rows;
    private int paletteCell;
    private int paletteW;
    private int leftColW;
    private int panelW, panelH;
    private int previewSize;

    private final Screen parent;
    private Mode mode;
    private String iconId;
    // Full-RGB pixel grid — one 0xRRGGBB entry per cell, no 16-color cap. Guild.flagPixelData
    // stores the same values as 6 hex chars each; see GuildFlagPixelArt.colorAt/toHex6.
    private final int[] pixelColors = new int[GuildFlagPixelArt.MAX_DIM * GuildFlagPixelArt.MAX_DIM];
    private int selectedColor = 0x000000;
    private int flagWidth;
    private int flagHeight;
    private int sizeIndex;

    private boolean syncingColorFields;
    private ChannelSlider rSlider, gSlider, bSlider;
    private EditBox hexBox;

    private int px, py;
    private int gridX, gridY, gridW, gridH;
    private int paletteX, paletteY;
    private int previewX, previewY;
    private int resLabelX, resLabelY;
    private int rightColX;

    public GuildFlagEditorScreen(Screen parent) {
        super(Component.literal("Guild Flag Editor"));
        this.parent = parent;
        this.mode = ClientGuildCache.flagUseDrawing ? Mode.DRAWING : Mode.ICON;
        this.iconId = ClientGuildCache.flagIconId;

        // Snap whatever was cached (which may predate the preset system, or may have drifted
        // width != height under an earlier free-form stepper system) onto the nearest preset,
        // defaulting to the middle size (32) if nothing's close.
        int cachedSize = Math.max(ClientGuildCache.flagWidth, ClientGuildCache.flagHeight);
        int bestIndex = 1;
        int bestDiff = Integer.MAX_VALUE;
        for (int i = 0; i < FLAG_SIZES.length; i++) {
            int diff = Math.abs(FLAG_SIZES[i] - cachedSize);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestIndex = i;
            }
        }
        this.sizeIndex = bestIndex;
        this.flagWidth = FLAG_SIZES[sizeIndex];
        this.flagHeight = FLAG_SIZES[sizeIndex];

        String data = ClientGuildCache.flagPixelData;
        boolean longEnough = data != null && data.length() >= pixelColors.length * GuildFlagPixelArt.CHARS_PER_PIXEL;
        for (int i = 0; i < pixelColors.length; i++) {
            pixelColors[i] = longEnough ? GuildFlagPixelArt.colorAt(data, i) : 0xFFFFFF;
        }
    }

    @Override
    protected void init() {
        cols = GuildFlagPixelArt.cols(flagWidth);
        rows = GuildFlagPixelArt.rows(flagHeight);

        previewSize = PREVIEW_SIZE;

        // Right column's content is the same fixed stack of rows in both modes (mode toggle,
        // size button, resolution label, one extra row that's Clear in drawing mode or Choose
        // Item/Block in icon mode, Save/Cancel) — computed once so it can be compared against the
        // left column's height to find the panel's true height, rather than summing both columns
        // the way the old single-stack layout did. Drawing mode gets two more rows below Clear
        // (the RGB sliders, and the hex code box) that icon mode has no use for.
        int rightH = ROW_H + ROW_GAP // mode toggle
                + ROW_H + ROW_GAP // size button
                + TITLE_H + ROW_GAP // resolution label
                + ROW_H + ROW_GAP; // clear / choose item
        if (mode == Mode.DRAWING) {
            rightH += ROW_H + ROW_GAP // R/G/B sliders
                    + ROW_H + ROW_GAP; // hex code box
        }
        rightH += ROW_H; // save/cancel

        if (mode == Mode.DRAWING) {
            // Cell size is capped so the grid's LARGER dimension never exceeds GRID_MAX, full
            // stop — see the class doc for why a fixed cap replaced an earlier fit-to-available-
            // space calculation.
            cell = Mth.clamp(GRID_MAX / Math.max(cols, rows), 2, 16);
            paletteCell = Math.max(cell, 18) + 2;
            gridW = cols * cell;
            gridH = rows * cell;
            paletteW = PALETTE_COLS * paletteCell;
            leftColW = Math.max(gridW, paletteW);
            int leftH = gridH + ROW_GAP + PALETTE_ROWS * paletteCell;
            panelH = PAD + TITLE_H + ROW_GAP + Math.max(leftH, rightH) + PAD;
        } else {
            leftColW = previewSize;
            panelH = PAD + TITLE_H + ROW_GAP + Math.max(previewSize, rightH) + PAD;
        }

        panelW = PAD + leftColW + COL_GAP + RIGHT_COL_W + PAD;

        // Defensive floor/ceiling on top of the above — belt-and-suspenders against any
        // remaining edge case (extreme aspect ratios, rounding) landing the panel partly
        // offscreen instead of just tightly packed.
        panelW = Math.min(panelW, width - 20);
        panelH = Math.min(panelH, height - 20);

        px = Math.max(10, (width - panelW) / 2);
        py = Math.max(10, (height - panelH) / 2);

        int contentTop = py + PAD + TITLE_H + ROW_GAP;
        int leftX = px + PAD;
        rightColX = px + PAD + leftColW + COL_GAP;

        // --- Left column: canvas ---
        if (mode == Mode.DRAWING) {
            gridX = leftX + (leftColW - gridW) / 2;
            gridY = contentTop;
            paletteX = leftX + (leftColW - paletteW) / 2;
            paletteY = gridY + gridH + ROW_GAP;
        } else {
            previewX = leftX;
            previewY = contentTop;
        }

        // --- Right column: controls ---
        int ry = contentTop;
        addRenderableWidget(Button.builder(Component.literal(mode == Mode.ICON ? "Mode: Item/Block" : "Mode: Draw"),
                b -> {
                    mode = mode == Mode.ICON ? Mode.DRAWING : Mode.ICON;
                    rebuildWidgets();
                }).bounds(rightColX, ry, RIGHT_COL_W, ROW_H).build());
        ry += ROW_H + ROW_GAP;

        addRenderableWidget(Button.builder(
                Component.literal("Size: " + flagWidth + "x" + flagHeight),
                b -> {
                    sizeIndex = (sizeIndex + 1) % FLAG_SIZES.length;
                    flagWidth = FLAG_SIZES[sizeIndex];
                    flagHeight = FLAG_SIZES[sizeIndex];
                    rebuildWidgets();
                }).bounds(rightColX, ry, RIGHT_COL_W, ROW_H).build());
        ry += ROW_H + ROW_GAP;

        resLabelX = rightColX + RIGHT_COL_W / 2;
        resLabelY = ry;
        ry += TITLE_H + ROW_GAP;

        if (mode == Mode.DRAWING) {
            addRenderableWidget(Button.builder(Component.literal("Clear"),
                    b -> Arrays.fill(pixelColors, 0xFFFFFF)).bounds(rightColX, ry, RIGHT_COL_W, ROW_H).build());
        } else {
            addRenderableWidget(Button.builder(Component.literal("Choose Item/Block"),
                    b -> Minecraft.getInstance().setScreen(new GuildFlagIconPickerScreen(this, iconId, picked -> {
                        iconId = picked;
                    })))
                    .bounds(rightColX, ry, RIGHT_COL_W, ROW_H).build());
        }
        ry += ROW_H + ROW_GAP;

        if (mode == Mode.DRAWING) {
            int sliderGap = 4;
            int sliderW = (RIGHT_COL_W - 2 * sliderGap) / 3;
            rSlider = addRenderableWidget(new ChannelSlider(rightColX, ry, sliderW, ROW_H, 'R', 16));
            gSlider = addRenderableWidget(
                    new ChannelSlider(rightColX + sliderW + sliderGap, ry, sliderW, ROW_H, 'G', 8));
            bSlider = addRenderableWidget(
                    new ChannelSlider(rightColX + 2 * (sliderW + sliderGap), ry, sliderW, ROW_H, 'B', 0));
            ry += ROW_H + ROW_GAP;

            hexBox = new EditBox(font, rightColX, ry, RIGHT_COL_W, ROW_H, Component.literal("Hex"));
            hexBox.setMaxLength(6);
            hexBox.setValue(GuildFlagPixelArt.toHex6(selectedColor));
            hexBox.setResponder(s -> {
                if (syncingColorFields || !s.matches("(?i)[0-9a-f]{6}")) return;
                selectedColor = Integer.parseInt(s, 16);
                rSlider.refresh();
                gSlider.refresh();
                bSlider.refresh();
            });
            addRenderableWidget(hexBox);
            ry += ROW_H + ROW_GAP;
        } else {
            rSlider = null;
            gSlider = null;
            bSlider = null;
            hexBox = null;
        }

        int halfW = (RIGHT_COL_W - 6) / 2;
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> save())
                .bounds(rightColX, ry, halfW, ROW_H).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose())
                .bounds(rightColX + halfW + 6, ry, halfW, ROW_H).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, C_BG);
        g.fill(px, py, px + panelW, py + panelH, C_PANEL);
        g.fill(px, py, px + panelW, py + 1, C_BORDER);
        g.fill(px, py + panelH - 1, px + panelW, py + panelH, C_BORDER);
        g.fill(px, py, px + 1, py + panelH, C_BORDER);
        g.fill(px + panelW - 1, py, px + panelW, py + panelH, C_BORDER);
        g.drawCenteredString(font, "Guild Flag Editor", px + panelW / 2, py + PAD, C_ACCENT);

        if (mode == Mode.DRAWING) {
            renderDrawingGrid(g);
        } else {
            g.fill(previewX - 2, previewY - 2, previewX + previewSize + 2, previewY + previewSize + 2, C_BORDER);
            GuildFlagIconManager.renderFlag(g, iconId, previewX, previewY, previewSize, previewSize);
        }

        String resLabel = "Resolution: " + flagWidth + "×" + flagHeight;
        g.drawCenteredString(font, resLabel, resLabelX, resLabelY, C_DIM);

        super.render(g, mx, my, pt);
    }

    private void renderDrawingGrid(GuiGraphics g) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int idx = row * GuildFlagPixelArt.MAX_DIM + col;
                int color = 0xFF000000 | pixelColors[idx];
                int cx = gridX + col * cell;
                int cy = gridY + row * cell;
                g.fill(cx, cy, cx + cell, cy + cell, color);
                g.fill(cx, cy, cx + cell, cy + 1, 0x33000000);
                g.fill(cx, cy, cx + 1, cy + cell, 0x33000000);
            }
        }
        g.fill(gridX + gridW, gridY, gridX + gridW + 1, gridY + gridH, 0x33000000);
        g.fill(gridX, gridY + gridH, gridX + gridW + 1, gridY + gridH + 1, 0x33000000);

        // Quick-pick swatches — a starting point, not the only colors available; the sliders/hex
        // box below let the selected color drift away from all sixteen of these.
        for (int i = 0; i < 16; i++) {
            int col = i % PALETTE_COLS;
            int row = i / PALETTE_COLS;
            int cx = paletteX + col * paletteCell;
            int cy = paletteY + row * paletteCell;
            g.fill(cx, cy, cx + paletteCell, cy + paletteCell, 0xFF000000 | GuildFlagPixelArt.PALETTE[i]);
            if (GuildFlagPixelArt.PALETTE[i] == selectedColor) {
                g.fill(cx, cy, cx + paletteCell, cy + 1, 0xFFFFFFFF);
                g.fill(cx, cy + paletteCell - 1, cx + paletteCell, cy + paletteCell, 0xFFFFFFFF);
                g.fill(cx, cy, cx + 1, cy + paletteCell, 0xFFFFFFFF);
                g.fill(cx + paletteCell - 1, cy, cx + paletteCell, cy + paletteCell, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (mode == Mode.DRAWING) {
            for (int i = 0; i < 16; i++) {
                int col = i % PALETTE_COLS;
                int row = i / PALETTE_COLS;
                int cx = paletteX + col * paletteCell;
                int cy = paletteY + row * paletteCell;
                if (mx >= cx && mx < cx + paletteCell && my >= cy && my < cy + paletteCell) {
                    selectedColor = GuildFlagPixelArt.PALETTE[i];
                    syncColorFields();
                    return true;
                }
            }
            if (mx >= gridX && mx < gridX + gridW && my >= gridY && my < gridY + gridH) {
                drawPixelAt(mx, my, btn == 1);
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    /** Pushes {@link #selectedColor} into the sliders + hex box without a full rebuildWidgets(). */
    private void syncColorFields() {
        if (rSlider == null) return;
        rSlider.refresh();
        gSlider.refresh();
        bSlider.refresh();
        syncHexBoxFromColor();
    }

    private void syncHexBoxFromColor() {
        if (hexBox == null) return;
        syncingColorFields = true;
        hexBox.setValue(GuildFlagPixelArt.toHex6(selectedColor));
        syncingColorFields = false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (mode == Mode.DRAWING && mx >= gridX && mx < gridX + gridW && my >= gridY && my < gridY + gridH) {
            drawPixelAt(mx, my, btn == 1);
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    private void drawPixelAt(double mx, double my, boolean erasing) {
        int col = (int) (mx - gridX) / cell;
        int row = (int) (my - gridY) / cell;
        if (col >= 0 && col < cols && row >= 0 && row < rows) {
            pixelColors[row * GuildFlagPixelArt.MAX_DIM + col] = erasing ? 0xFFFFFF : selectedColor;
        }
    }

    private void save() {
        StringBuilder sb = new StringBuilder(pixelColors.length * GuildFlagPixelArt.CHARS_PER_PIXEL);
        for (int color : pixelColors) sb.append(GuildFlagPixelArt.toHex6(color));
        GuildNetwork.CHANNEL.sendToServer(
                new C2SSetGuildFlagPacket(mode == Mode.DRAWING, iconId, sb.toString(), flagWidth, flagHeight));
        onClose();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * One R/G/B channel of {@link #selectedColor} (0-255 mapped onto {@code AbstractSliderButton}'s
     * normalized 0..1 {@code value}), matching the {@code SaturationSlider}/{@code ContrastSlider}
     * pattern already established in Solaris's own settings screen. A non-static inner class so it
     * can read/write the outer {@link #selectedColor} field directly rather than needing a
     * get/set-callback pair threaded through the constructor.
     */
    private final class ChannelSlider extends AbstractSliderButton {

        private final char channel;
        private final int shift;

        ChannelSlider(int x, int y, int w, int h, char channel, int shift) {
            super(x, y, w, h, Component.empty(), ((selectedColor >> shift) & 0xFF) / 255.0);
            this.channel = channel;
            this.shift = shift;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(channel + ":" + Math.round(value * 255)));
        }

        @Override
        protected void applyValue() {
            int v = (int) Math.round(value * 255);
            int mask = 0xFF << shift;
            selectedColor = (selectedColor & ~mask) | (v << shift);
            syncHexBoxFromColor();
        }

        /** Re-reads {@link #selectedColor} after an external change (swatch click, hex box edit). */
        void refresh() {
            this.value = ((selectedColor >> shift) & 0xFF) / 255.0;
            updateMessage();
        }
    }
}
