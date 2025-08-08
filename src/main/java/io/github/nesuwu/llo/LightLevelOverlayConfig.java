package io.github.nesuwu.llo;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class LightLevelOverlayConfig {
    private LightLevelOverlayConfig() {}

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    private static final String CATEGORY = "client";

    public static ModConfigSpec.IntValue rangeHorizontal;
    public static ModConfigSpec.IntValue rangeVertical;
    public static ModConfigSpec.LongValue updateIntervalMs;

    static {
        BUILDER.push(CATEGORY);

        rangeHorizontal = BUILDER
                .comment("Horizontal scan radius in blocks for light level overlay")
                .defineInRange("rangeHorizontal", 16, 1, 128);

        rangeVertical = BUILDER
                .comment("Vertical scan half-height in blocks for per-column search")
                .defineInRange("rangeVertical", 8, 1, 64);

        updateIntervalMs = BUILDER
                .comment("Milliseconds between overlay cache refreshes")
                .defineInRange("updateIntervalMs", 150L, 16L, 2000L);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}


