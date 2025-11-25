package io.github.nesuwu.llo;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

public final class LightCache {

    private final Long2IntMap cache = new Long2IntOpenHashMap();
    private long lastUpdateTimeMs = 0;

    public LightCache() {
        cache.defaultReturnValue(-1);
    }

    public Long2IntMap getCache() {
        return cache;
    }

    public void clear() {
        cache.clear();
    }

    public void put(long packedPos, int lightLevel) {
        cache.put(packedPos, lightLevel);
    }

    public int size() {
        return cache.size();
    }

    public long getLastUpdateTimeMs() {
        return lastUpdateTimeMs;
    }

    public void setLastUpdateTimeMs(long timeMs) {
        this.lastUpdateTimeMs = timeMs;
    }

    public boolean shouldUpdate(long currentTimeMs, long intervalMs) {
        return (currentTimeMs - lastUpdateTimeMs) > intervalMs;
    }
}
