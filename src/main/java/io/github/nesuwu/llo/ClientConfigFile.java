package io.github.nesuwu.llo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class ClientConfigFile {
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "lightleveloverlay-client.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile boolean loaded = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConfigFile.class);

    public static class Data {
        public int rangeHorizontal = 16;
        public int rangeVertical = 8;
        public long updateIntervalMs = 150L;
        public int colorZero = 0xFF4040;
        public int colorLow = 0xFFFF40;
        public int colorSafe = 0x40FF40;
        public boolean showOnlyUnsafe = false;
        public boolean showOnlySpawnable = false;
        public double textScale = 0.025d;
    }

    private static Data data = new Data();

    private ClientConfigFile() {}

    public static void ensureLoaded() {
        if (loaded) return;
        load();
    }

    public static int getRangeHorizontal() {
        ensureLoaded();
        return Math.max(1, Math.min(128, data.rangeHorizontal));
    }

    public static int getRangeVertical() {
        ensureLoaded();
        return Math.max(1, Math.min(64, data.rangeVertical));
    }

    public static long getUpdateIntervalMs() {
        ensureLoaded();
        long clamped = Math.max(16L, Math.min(2000L, data.updateIntervalMs));
        return clamped;
    }

    public static void updateAndSave(int rangeH, int rangeV, long intervalMs) {
        ensureLoaded();
        data.rangeHorizontal = Math.max(1, Math.min(128, rangeH));
        data.rangeVertical = Math.max(1, Math.min(64, rangeV));
        data.updateIntervalMs = Math.max(16L, Math.min(2000L, intervalMs));
        save();
    }

    public static void load() {
        File dir = FMLPaths.CONFIGDIR.get().toFile();
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        File file = new File(dir, CONFIG_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                Data read = GSON.fromJson(reader, Data.class);
                if (read != null) {
                    data = read;
                }
            } catch (IOException | JsonSyntaxException ex) {
                // Fallback to defaults on error
                LOGGER.warn("Failed to read config file {}, falling back to defaults: {}", file.getAbsolutePath(), ex.toString());
                data = new Data();
            }
        } else {
            save();
        }
        // One-time migration: prefer new field and clear legacy flag if needed
        if (data.showOnlyUnsafe && !data.showOnlySpawnable) {
            data.showOnlySpawnable = true;
            data.showOnlyUnsafe = false;
            LOGGER.info("Migrated legacy showOnlyUnsafe=true to showOnlySpawnable=true in {}", new File(dir, CONFIG_FILE).getAbsolutePath());
            save();
        }
        loaded = true;
    }

    public static void save() {
        File dir = FMLPaths.CONFIGDIR.get().toFile();
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        File file = new File(dir, CONFIG_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(GSON.toJson(data));
        } catch (IOException ex) {
            LOGGER.error("Failed to write config file {}: {}", file.getAbsolutePath(), ex.toString());
        }
    }

    // Getters with clamping (store RGB only; alpha is added at render time)
    public static int getColorZero() { ensureLoaded(); return clampRgb(data.colorZero); }
    public static int getColorLow() { ensureLoaded(); return clampRgb(data.colorLow); }
    public static int getColorSafe() { ensureLoaded(); return clampRgb(data.colorSafe); }
    public static boolean isShowOnlySpawnable() { ensureLoaded(); return data.showOnlySpawnable || data.showOnlyUnsafe; }
    public static double getTextScale() { ensureLoaded(); return Math.max(0.015d, Math.min(0.06d, data.textScale)); }

    private static int clampRgb(int rgb) { return rgb & 0x00FFFFFF; }

    // Field updaters used by Cloth Config
    public static void setRangeHorizontal(int v) { ensureLoaded(); data.rangeHorizontal = Math.max(1, Math.min(128, v)); save(); }
    public static void setRangeVertical(int v) { ensureLoaded(); data.rangeVertical = Math.max(1, Math.min(64, v)); save(); }
    public static void setUpdateIntervalMs(long v) { ensureLoaded(); data.updateIntervalMs = Math.max(16L, Math.min(2000L, v)); save(); }
    public static void setColorZero(int v) { ensureLoaded(); data.colorZero = clampRgb(v); LOGGER.info("Config updated: colorZero=#{}", String.format("%06X", data.colorZero)); save(); }
    public static void setColorLow(int v) { ensureLoaded(); data.colorLow = clampRgb(v); LOGGER.info("Config updated: colorLow=#{}", String.format("%06X", data.colorLow)); save(); }
    public static void setColorSafe(int v) { ensureLoaded(); data.colorSafe = clampRgb(v); LOGGER.info("Config updated: colorSafe=#{}", String.format("%06X", data.colorSafe)); save(); }
    public static void setShowOnlySpawnable(boolean v) {
        ensureLoaded();
        data.showOnlySpawnable = v;
        // Keep legacy field in sync so older versions respect the change
        data.showOnlyUnsafe = v;
        LOGGER.info("Config updated: showOnlySpawnable={} (legacy showOnlyUnsafe={})", v, v);
        save();
    }
    public static void setTextScale(double v) { ensureLoaded(); data.textScale = Math.max(0.015d, Math.min(0.06d, v)); save(); }
}


