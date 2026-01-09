package io.github.nesuwu.llo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.architectury.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientConfig {

    private static final String CONFIG_FILE = "lightleveloverlay-client.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConfig.class);
    private static Data data;

    public static class Data {
        public int rangeHorizontal = 16;
        public int rangeVertical = 8;
        public long updateIntervalMs = 150L;
        public int colorZero = 0xFF4040;
        public int colorLow = 0xFFFF40;
        public int colorSafe = 0x40FF40;
        public boolean showOnlySpawnable = false;
        public double textScale = 0.025d;
        public boolean enableUnderwaterMode = false;
        public String underwaterDisplayMode = "Both";
        public int colorUnderwater = 0xFF8040;
    }

    private ClientConfig() {
    }

    public static void ensureLoaded() {
        if (data == null)
            load();
    }

    private static void load() {
        Path configDir = Platform.getConfigFolder();
        Path file = configDir.resolve(CONFIG_FILE);

        if (Files.exists(file)) {
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                Data read = GSON.fromJson(reader, Data.class);
                data = read != null ? read : new Data();
            } catch (IOException | JsonSyntaxException ex) {
                LOGGER.warn("Failed to read config: {}", ex.toString());
                data = new Data();
            }
        } else {
            data = new Data();
            save();
        }
    }

    public static void save() {
        if (data == null)
            data = new Data();
        Path configDir = Platform.getConfigFolder();
        try {
            Files.createDirectories(configDir);
            Path file = configDir.resolve(CONFIG_FILE);
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                writer.write(GSON.toJson(data));
            }
        } catch (IOException ex) {
            LOGGER.error("Failed to write config: {}", ex.toString());
        }
    }

    public static int getRangeHorizontal() {
        ensureLoaded();
        return clamp(data.rangeHorizontal, 1, 128);
    }

    public static int getRangeVertical() {
        ensureLoaded();
        return clamp(data.rangeVertical, 1, 64);
    }

    public static long getUpdateIntervalMs() {
        ensureLoaded();
        return clamp(data.updateIntervalMs, 16L, 2000L);
    }

    public static int getColorZero() {
        ensureLoaded();
        return data.colorZero & 0x00FFFFFF;
    }

    public static int getColorLow() {
        ensureLoaded();
        return data.colorLow & 0x00FFFFFF;
    }

    public static int getColorSafe() {
        ensureLoaded();
        return data.colorSafe & 0x00FFFFFF;
    }

    public static int getColorUnderwater() {
        ensureLoaded();
        return data.colorUnderwater & 0x00FFFFFF;
    }

    public static boolean isShowOnlySpawnable() {
        ensureLoaded();
        return data.showOnlySpawnable;
    }

    public static double getTextScale() {
        ensureLoaded();
        return clamp(data.textScale, 0.015d, 0.06d);
    }

    public static boolean isUnderwaterModeEnabled() {
        ensureLoaded();
        return data.enableUnderwaterMode;
    }

    public static String getUnderwaterDisplayMode() {
        ensureLoaded();
        String mode = data.underwaterDisplayMode;
        return "Floor".equals(mode) || "Surface".equals(mode) || "Both".equals(mode) ? mode : "Both";
    }

    public static void setRangeHorizontal(int v) {
        ensureLoaded();
        data.rangeHorizontal = clamp(v, 1, 128);
    }

    public static void setRangeVertical(int v) {
        ensureLoaded();
        data.rangeVertical = clamp(v, 1, 64);
    }

    public static void setUpdateIntervalMs(long v) {
        ensureLoaded();
        data.updateIntervalMs = clamp(v, 16L, 2000L);
    }

    public static void setColorZero(int v) {
        ensureLoaded();
        data.colorZero = v & 0x00FFFFFF;
    }

    public static void setColorLow(int v) {
        ensureLoaded();
        data.colorLow = v & 0x00FFFFFF;
    }

    public static void setColorSafe(int v) {
        ensureLoaded();
        data.colorSafe = v & 0x00FFFFFF;
    }

    public static void setColorUnderwater(int v) {
        ensureLoaded();
        data.colorUnderwater = v & 0x00FFFFFF;
    }

    public static void setShowOnlySpawnable(boolean v) {
        ensureLoaded();
        data.showOnlySpawnable = v;
    }

    public static void setTextScale(double v) {
        ensureLoaded();
        data.textScale = clamp(v, 0.015d, 0.06d);
    }

    public static void setUnderwaterModeEnabled(boolean v) {
        ensureLoaded();
        data.enableUnderwaterMode = v;
    }

    public static void setUnderwaterDisplayMode(String v) {
        ensureLoaded();
        data.underwaterDisplayMode = "Floor".equals(v) || "Surface".equals(v) || "Both".equals(v) ? v : "Both";
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private static long clamp(long val, long min, long max) {
        return Math.max(min, Math.min(max, val));
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
