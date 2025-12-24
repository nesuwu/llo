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
                int startRh = ClientConfigFile.getRangeHorizontal();
                int startRv = ClientConfigFile.getRangeVertical();
                long startUi = ClientConfigFile.getUpdateIntervalMs();
                int startZero = ClientConfigFile.getColorZero();
                int startLow = ClientConfigFile.getColorLow();
                int startSafe = ClientConfigFile.getColorSafe();
                boolean startSpawnableOnly = ClientConfigFile.isShowOnlySpawnable();
                double startScale = ClientConfigFile.getTextScale();

                ConfigBuilder builder = ConfigBuilder.create()
                                .setParentScreen(parent)
                                .setTitle(Component.literal("Light Level Overlay"));

                builder.setSavingRunnable(ClientConfigFile::save);

                ConfigEntryBuilder eb = builder.entryBuilder();
                ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
                ConfigCategory visual = builder.getOrCreateCategory(Component.literal("Visuals"));

                general.addEntry(eb.startIntField(Component.literal("Horizontal Range"), startRh)
                                .setDefaultValue(16)
                                .setMin(1)
                                .setMax(128)
                                .setTooltip(Component.literal("XZ radius in blocks scanned for surfaces (1-128)."))
                                .setSaveConsumer(ClientConfigFile::setRangeHorizontal)
                                .build());

                general.addEntry(eb.startIntField(Component.literal("Vertical Range"), startRv)
                                .setDefaultValue(8)
                                .setMin(1)
                                .setMax(64)
                                .setTooltip(Component.literal(
                                                "Half-height in blocks above/below player to find top surface (1-64)."))
                                .setSaveConsumer(ClientConfigFile::setRangeVertical)
                                .build());

                general.addEntry(eb.startLongField(Component.literal("Update Interval (ms)"), startUi)
                                .setDefaultValue(150L)
                                .setMin(16L)
                                .setMax(2000L)
                                .setTooltip(Component.literal(
                                                "Milliseconds between overlay cache refreshes (16-2000). Higher = less CPU."))
                                .setSaveConsumer(ClientConfigFile::setUpdateIntervalMs)
                                .build());

                visual.addEntry(eb.startColorField(Component.literal("Color: Light Level 0"), startZero)
                                .setDefaultValue(0xFF4040)
                                .setAlphaMode(false)
                                .setTooltip(Component.literal("RGB color for light level 0 (unsafe)."))
                                .setSaveConsumer(ClientConfigFile::setColorZero)
                                .build());

                visual.addEntry(eb.startColorField(Component.literal("Color: Light Level 1-7"), startLow)
                                .setDefaultValue(0xFFFF40)
                                .setAlphaMode(false)
                                .setTooltip(Component.literal(
                                                "RGB color for low light levels below the game-safe value (1-7)."))
                                .setSaveConsumer(ClientConfigFile::setColorLow)
                                .build());

                visual.addEntry(eb.startColorField(Component.literal("Color: Safe (>= 8)"), startSafe)
                                .setDefaultValue(0x40FF40)
                                .setAlphaMode(false)
                                .setTooltip(Component.literal("RGB color for safe light levels."))
                                .setSaveConsumer(ClientConfigFile::setColorSafe)
                                .build());

                visual.addEntry(eb.startBooleanToggle(Component.literal("Show Only Spawnable"), startSpawnableOnly)
                                .setDefaultValue(false)
                                .setTooltip(Component
                                                .literal("If enabled, only shows blocks where mobs can spawn (0-7)."))
                                .setSaveConsumer(ClientConfigFile::setShowOnlySpawnable)
                                .build());

                visual.addEntry(eb.startDoubleField(Component.literal("Text Scale"), startScale)
                                .setDefaultValue(0.025d)
                                .setMin(0.015d)
                                .setMax(0.06d)
                                .setTooltip(Component.literal("Scales overlay text size."))
                                .setSaveConsumer(ClientConfigFile::setTextScale)
                                .build());

                boolean startUnderwaterEnabled = ClientConfigFile.isUnderwaterModeEnabled();
                String startUnderwaterMode = ClientConfigFile.getUnderwaterDisplayMode();
                int startColorUnderwater = ClientConfigFile.getColorUnderwater();

                ConfigCategory underwater = builder.getOrCreateCategory(Component.literal("Underwater"));

                underwater.addEntry(eb
                                .startBooleanToggle(Component.literal("Enable Underwater Mode"), startUnderwaterEnabled)
                                .setDefaultValue(false)
                                .setTooltip(Component
                                                .literal("Show light levels in water for drowned spawn prevention."))
                                .setSaveConsumer(ClientConfigFile::setUnderwaterModeEnabled)
                                .build());

                underwater.addEntry(eb
                                .startSelector(Component.literal("Underwater Display"),
                                                new String[] { "Floor", "Surface", "Both" }, startUnderwaterMode)
                                .setDefaultValue("Both")
                                .setTooltip(Component.literal(
                                                "Floor = Show on riverbed. Surface = Show at water top. Both = Auto-switch based on player position."))
                                .setSaveConsumer(ClientConfigFile::setUnderwaterDisplayMode)
                                .build());

                underwater.addEntry(eb.startColorField(Component.literal("Color: Underwater"), startColorUnderwater)
                                .setDefaultValue(0xFF8040)
                                .setAlphaMode(false)
                                .setTooltip(Component.literal("RGB color for underwater spawn zones."))
                                .setSaveConsumer(ClientConfigFile::setColorUnderwater)
                                .build());

                return builder.build();
        }
}
