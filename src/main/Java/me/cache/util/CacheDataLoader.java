package me.cache.util;

import me.cache.util.exception.CacheLoadingException;

@FunctionalInterface
public interface CacheDataLoader<K, V> {
    V loadData(K k) throws CacheLoadingException;
}
