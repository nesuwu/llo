package io.github.nesuwu.llo;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.Font;

import java.util.ArrayList;
import java.util.List;

public class SimpleConfigScreen extends Screen {
    private final Screen parent;
    private EditBox rangeHorizontalBox;
    private EditBox rangeVerticalBox;
    private EditBox updateIntervalBox;
    private StringWidget labelRh;
    private StringWidget labelRv;
    private StringWidget labelUi;
    private Button doneButton;
    private Button discardButton;

    private boolean validRh = true;
    private boolean validRv = true;
    private boolean validUi = true;

    private final List<RowTooltip> tooltips = new ArrayList<>();

    private int startRh;
    private int startRv;
    private long startUi;

    public SimpleConfigScreen(Screen parent) {
        super(Component.literal("Light Level Overlay - Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        startRh = ClientConfigFile.getRangeHorizontal();
        startRv = ClientConfigFile.getRangeVertical();
        startUi = ClientConfigFile.getUpdateIntervalMs();

        GridLayout form = new GridLayout();
        form.defaultCellSetting().padding(4);
        GridLayout.RowHelper f = form.createRowHelper(5);

        int inputWidth = 180;
        int resetWidth = 60;
        int stepWidth = 18;

        labelRh = new StringWidget(Component.literal("Horizontal Range"), this.font);
        f.addChild(labelRh);
        rangeHorizontalBox = new EditBox(this.font, 0, 0, inputWidth, 20, Component.empty());
        rangeHorizontalBox.setValue(String.valueOf(startRh));
        rangeHorizontalBox.setResponder(s -> updateValidity());
        f.addChild(rangeHorizontalBox, LayoutSettings.defaults().alignHorizontallyCenter());
        f.addChild(Button.builder(Component.literal("-"), b -> nudge(rangeHorizontalBox, -1, 1, 128)).size(stepWidth, 20).build());
        f.addChild(Button.builder(Component.literal("+"), b -> nudge(rangeHorizontalBox, +1, 1, 128)).size(stepWidth, 20).build());
        f.addChild(Button.builder(Component.literal("Reset"), b -> {
            rangeHorizontalBox.setValue("16");
        }).size(resetWidth, 20).build());

        labelRv = new StringWidget(Component.literal("Vertical Range"), this.font);
        f.addChild(labelRv);
        rangeVerticalBox = new EditBox(this.font, 0, 0, inputWidth, 20, Component.empty());
        rangeVerticalBox.setValue(String.valueOf(startRv));
        rangeVerticalBox.setResponder(s -> updateValidity());
        f.addChild(rangeVerticalBox, LayoutSettings.defaults().alignHorizontallyCenter());
        f.addChild(Button.builder(Component.literal("-"), b -> nudge(rangeVerticalBox, -1, 1, 64)).size(stepWidth, 20).build());
        f.addChild(Button.builder(Component.literal("+"), b -> nudge(rangeVerticalBox, +1, 1, 64)).size(stepWidth, 20).build());
        f.addChild(Button.builder(Component.literal("Reset"), b -> {
            rangeVerticalBox.setValue("8");
        }).size(resetWidth, 20).build());

        labelUi = new StringWidget(Component.literal("Update Interval (ms)"), this.font);
        f.addChild(labelUi);
        updateIntervalBox = new EditBox(this.font, 0, 0, inputWidth, 20, Component.empty());
        updateIntervalBox.setValue(String.valueOf(startUi));
        updateIntervalBox.setResponder(s -> updateValidity());
        f.addChild(updateIntervalBox, LayoutSettings.defaults().alignHorizontallyCenter());
        f.addChild(Button.builder(Component.literal("-"), b -> nudgeLong(updateIntervalBox, -10, 16, 2000)).size(stepWidth, 20).build());
        f.addChild(Button.builder(Component.literal("+"), b -> nudgeLong(updateIntervalBox, +10, 16, 2000)).size(stepWidth, 20).build());
        f.addChild(Button.builder(Component.literal("Reset"), b -> {
            updateIntervalBox.setValue("150");
        }).size(resetWidth, 20).build());

        form.arrangeElements();
        FrameLayout.alignInRectangle(form, 0, 0, this.width, this.height, 0.5f, 0.33f);
        form.visitWidgets(this::addRenderableWidget);

        tooltips.clear();
        tooltips.add(new RowTooltip(boundsForRow(labelRh, rangeHorizontalBox), Component.literal("XZ radius in blocks scanned for surfaces (1-128).")));
        tooltips.add(new RowTooltip(boundsForRow(labelRv, rangeVerticalBox), Component.literal("Half-height in blocks above/below player to find top surface (1-64).")));
        tooltips.add(new RowTooltip(boundsForRow(labelUi, updateIntervalBox), Component.literal("Milliseconds between overlay cache refreshes (16-2000). Higher = less CPU.")));

        GridLayout actions = new GridLayout();
        actions.defaultCellSetting().padding(4);
        GridLayout.RowHelper ar = actions.createRowHelper(2);
        discardButton = Button.builder(Component.literal("Discard Changes"), b -> {
            rangeHorizontalBox.setValue(String.valueOf(startRh));
            rangeVerticalBox.setValue(String.valueOf(startRv));
            updateIntervalBox.setValue(String.valueOf(startUi));
            onClose();
        }).size(150, 20).build();
        doneButton = Button.builder(Component.literal("Done"), b -> saveAndClose()).size(120, 20).build();
        ar.addChild(discardButton);
        ar.addChild(doneButton, LayoutSettings.defaults().alignHorizontallyRight());
        actions.arrangeElements();
        FrameLayout.alignInRectangle(actions, 0, 0, this.width, this.height, 0.5f, 0.85f);
        actions.visitWidgets(this::addRenderableWidget);

        updateValidity();
    }

    private void saveAndClose() {
        try {
            int rh = Integer.parseInt(rangeHorizontalBox.getValue().trim());
            int rv = Integer.parseInt(rangeVerticalBox.getValue().trim());
            long ui = Long.parseLong(updateIntervalBox.getValue().trim());
            ClientConfigFile.updateAndSave(rh, rv, ui);
        } catch (NumberFormatException ignored) {}
        onClose();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);

        if (!validRh) drawInvalidOutline(gfx, rangeHorizontalBox);
        if (!validRv) drawInvalidOutline(gfx, rangeVerticalBox);
        if (!validUi) drawInvalidOutline(gfx, updateIntervalBox);

        for (RowTooltip rt : tooltips) {
            if (rt.isMouseOver(mouseX, mouseY)) {
                gfx.renderTooltip(this.font, rt.text, mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    private void updateValidity() {
        validRh = validateAndTint(rangeHorizontalBox, 1, 128);
        validRv = validateAndTint(rangeVerticalBox, 1, 64);
        validUi = validateAndTintLong(updateIntervalBox, 16L, 2000L);
        if (doneButton != null) {
            doneButton.active = validRh && validRv && validUi;
        }
    }

    private boolean validateAndTint(EditBox box, int min, int max) {
        try {
            int val = Integer.parseInt(box.getValue().trim());
            boolean ok = val >= min && val <= max;
            box.setTextColor(ok ? 0xE0E0E0 : 0xE04B4B);
            return ok;
        } catch (NumberFormatException ex) {
            box.setTextColor(0xE04B4B);
            return false;
        }
    }

    private boolean validateAndTintLong(EditBox box, long min, long max) {
        try {
            long val = Long.parseLong(box.getValue().trim());
            boolean ok = val >= min && val <= max;
            box.setTextColor(ok ? 0xE0E0E0 : 0xE04B4B);
            return ok;
        } catch (NumberFormatException ex) {
            box.setTextColor(0xE04B4B);
            return false;
        }
    }

    private void nudge(EditBox box, int delta, int min, int max) {
        try {
            int val = Integer.parseInt(box.getValue().trim());
            val = Math.max(min, Math.min(max, val + delta));
            box.setValue(Integer.toString(val));
        } catch (NumberFormatException ignored) {}
        updateValidity();
    }

    private void nudgeLong(EditBox box, long delta, long min, long max) {
        try {
            long val = Long.parseLong(box.getValue().trim());
            val = Math.max(min, Math.min(max, val + delta));
            box.setValue(Long.toString(val));
        } catch (NumberFormatException ignored) {}
        updateValidity();
    }

    private void drawInvalidOutline(GuiGraphics gfx, EditBox box) {
        int x = box.getX() - 1;
        int y = box.getY() - 1;
        int w = box.getWidth() + 2;
        int h = box.getHeight() + 2;
        int color = 0xAAE04B4B;
        gfx.fill(x, y, x + w, y + 1, color);
        gfx.fill(x, y + h - 1, x + w, y + h, color);
        gfx.fill(x, y, x + 1, y + h, color);
        gfx.fill(x + w - 1, y, x + w, y + h, color);
    }

    private Rect boundsForRow(StringWidget label, EditBox input) {
        int left = Math.min(label.getX(), input.getX());
        int top = Math.min(label.getY(), input.getY());
        int right = Math.max(label.getX() + label.getWidth(), input.getX() + input.getWidth());
        int bottom = Math.max(label.getY() + label.getHeight(), input.getY() + input.getHeight());
        return new Rect(left - 2, top - 2, right - left + 4, bottom - top + 4);
    }

    private record Rect(int x, int y, int w, int h) {}

    private static class RowTooltip {
        final Rect rect;
        final Component text;
        RowTooltip(Rect rect, Component text) { this.rect = rect; this.text = text; }
        boolean isMouseOver(int mx, int my) {
            return mx >= rect.x && my >= rect.y && mx < rect.x + rect.w && my < rect.y + rect.h;
        }
    }
}


