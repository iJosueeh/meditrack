package com.utp.meditrackapp.core.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ReferenceCacheManager {

    private static ReferenceCacheManager instance;

    public enum CacheType {
        TIPOS_MOVIMIENTO(24 * 60 * 60 * 1000),
        MOTIVOS_MOVIMIENTO(24 * 60 * 60 * 1000),
        CATEGORIAS(60 * 60 * 1000),
        PRODUCTOS(60 * 60 * 1000);

        private final long ttlMs;

        CacheType(long ttlMs) {
            this.ttlMs = ttlMs;
        }

        public long getTtlMs() {
            return ttlMs;
        }
    }

    private final Map<CacheType, CacheEntry<?>> cache = new ConcurrentHashMap<>();

    private ReferenceCacheManager() {}

    public static synchronized ReferenceCacheManager getInstance() {
        if (instance == null) {
            instance = new ReferenceCacheManager();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(CacheType type, Supplier<T> loader) {
        CacheEntry<?> entry = cache.get(type);
        if (entry != null && !entry.isExpired()) {
            return (T) entry.data;
        }
        T data = loader.get();
        cache.put(type, new CacheEntry<>(data, type.getTtlMs()));
        return data;
    }

    public void invalidate(CacheType... types) {
        for (CacheType type : types) {
            cache.remove(type);
        }
    }

    public void clearAll() {
        cache.clear();
    }

    private static class CacheEntry<T> {
        final T data;
        final long expiryTime;

        CacheEntry(T data, long ttlMs) {
            this.data = data;
            this.expiryTime = System.currentTimeMillis() + ttlMs;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
}
