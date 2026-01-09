package io.github.nesuwu.llo;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ClothConfigScreens {

    private ClothConfigScreens() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Light Level Overlay"));
        builder.setSavingRunnable(ClientConfig::save);

        ConfigEntryBuilder eb = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        ConfigCategory visual = builder.getOrCreateCategory(Component.literal("Visuals"));
        ConfigCategory underwater = builder.getOrCreateCategory(Component.literal("Underwater"));

        general.addEntry(eb.startIntField(Component.literal("Horizontal Range"), ClientConfig.getRangeHorizontal())
                .setDefaultValue(16).setMin(1).setMax(128)
                .setTooltip(Component.literal("XZ radius in blocks scanned for surfaces (1-128)."))
                .setSaveConsumer(ClientConfig::setRangeHorizontal).build());

        general.addEntry(eb.startIntField(Component.literal("Vertical Range"), ClientConfig.getRangeVertical())
                .setDefaultValue(8).setMin(1).setMax(64)
                .setTooltip(Component.literal("Half-height in blocks above/below player to find top surface (1-64)."))
                .setSaveConsumer(ClientConfig::setRangeVertical).build());

        general.addEntry(
                eb.startLongField(Component.literal("Update Interval (ms)"), ClientConfig.getUpdateIntervalMs())
                        .setDefaultValue(150L).setMin(16L).setMax(2000L)
                        .setTooltip(Component.literal("Milliseconds between overlay cache refreshes (16-2000)."))
                        .setSaveConsumer(ClientConfig::setUpdateIntervalMs).build());

        visual.addEntry(eb.startColorField(Component.literal("Color: Light Level 0"), ClientConfig.getColorZero())
                .setDefaultValue(0xFF4040).setAlphaMode(false)
                .setTooltip(Component.literal("RGB color for light level 0 (unsafe)."))
                .setSaveConsumer(ClientConfig::setColorZero).build());

        visual.addEntry(eb.startColorField(Component.literal("Color: Light Level 1-7"), ClientConfig.getColorLow())
                .setDefaultValue(0xFFFF40).setAlphaMode(false)
                .setTooltip(Component.literal("RGB color for low light levels below the game-safe value (1-7)."))
                .setSaveConsumer(ClientConfig::setColorLow).build());

        visual.addEntry(eb.startColorField(Component.literal("Color: Safe (>= 8)"), ClientConfig.getColorSafe())
                .setDefaultValue(0x40FF40).setAlphaMode(false)
                .setTooltip(Component.literal("RGB color for safe light levels."))
                .setSaveConsumer(ClientConfig::setColorSafe).build());

        visual.addEntry(
                eb.startBooleanToggle(Component.literal("Show Only Spawnable"), ClientConfig.isShowOnlySpawnable())
                        .setDefaultValue(false)
                        .setTooltip(Component.literal("If enabled, only shows blocks where mobs can spawn (0-7)."))
                        .setSaveConsumer(ClientConfig::setShowOnlySpawnable).build());

        visual.addEntry(eb.startDoubleField(Component.literal("Text Scale"), ClientConfig.getTextScale())
                .setDefaultValue(0.025d).setMin(0.015d).setMax(0.06d)
                .setTooltip(Component.literal("Scales overlay text size."))
                .setSaveConsumer(ClientConfig::setTextScale).build());

        underwater.addEntry(eb
                .startBooleanToggle(Component.literal("Enable Underwater Mode"), ClientConfig.isUnderwaterModeEnabled())
                .setDefaultValue(false)
                .setTooltip(Component.literal("Show light levels in water for drowned spawn prevention."))
                .setSaveConsumer(ClientConfig::setUnderwaterModeEnabled).build());

        underwater.addEntry(eb.startSelector(Component.literal("Underwater Display"),
                new String[] { "Floor", "Surface", "Both" }, ClientConfig.getUnderwaterDisplayMode())
                .setDefaultValue("Both")
                .setTooltip(
                        Component.literal("Floor = Show on riverbed. Surface = Show at water top. Both = Auto-switch."))
                .setSaveConsumer(ClientConfig::setUnderwaterDisplayMode).build());

        underwater
                .addEntry(eb.startColorField(Component.literal("Color: Underwater"), ClientConfig.getColorUnderwater())
                        .setDefaultValue(0xFF8040).setAlphaMode(false)
                        .setTooltip(Component.literal("RGB color for underwater spawn zones."))
                        .setSaveConsumer(ClientConfig::setColorUnderwater).build());

        return builder.build();
    }
}
