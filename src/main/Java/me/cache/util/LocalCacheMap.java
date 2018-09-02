package me.cache.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import me.cache.util.exception.CacheLoadingException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class LocalCacheMap<K,V> implements ICache<K,V> {
    public final int size;
    public final long expireAfterWrite;
    public final TimeUnit timeUnit;
    private final Cache<K, V> guavaCache;

    public LocalCacheMap(int size) {
        this.size = size;
        this.expireAfterWrite = 0;
        this.timeUnit = null;
        this.guavaCache = getGuavaCache(this.size);
    }

    public LocalCacheMap(int size, long expireAfterWrite, TimeUnit timeUnit) {
        this.size = size;
        this.expireAfterWrite = expireAfterWrite;
        this.timeUnit = timeUnit;
        this.guavaCache = getGuavaCache(this.size, this.expireAfterWrite, this.timeUnit);
    }

    private Cache getGuavaCache(int size) {
        return CacheBuilder.newBuilder().maximumSize(size)
                .expireAfterWrite(expireAfterWrite, timeUnit)
                .build();
    }

    private Cache getGuavaCache(int size, long expireAfterWrite, TimeUnit timeUnit) {
        return CacheBuilder.newBuilder().maximumSize(size)
                .expireAfterWrite(expireAfterWrite, timeUnit)
                .build();
    }

    @Override
    public Optional<V> get(K key) throws CacheLoadingException {
        return Optional.ofNullable(guavaCache.getIfPresent(key));
    }

    @Override
    public void put(K key, V value) {
        guavaCache.put(key, value);
    }

    @Override
    public void putAll(Map<K, V> map) {
        map.forEach((key, value)-> {
            put(key, value);
        });
    }

    @Override
    public ImmutableMap<K, V> asMap() {
        return ImmutableMap.copyOf(guavaCache.asMap());
    }
}