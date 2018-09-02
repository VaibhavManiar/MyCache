package me.cache.util;

import com.google.common.collect.ImmutableMap;
import me.cache.util.exception.CacheLoadingException;

import java.util.Map;
import java.util.Optional;

public interface ICache<K, V> {
    Optional<V> get(K key) throws CacheLoadingException;
    void put(K k, V v);
    void putAll(Map<K,V> map);
    ImmutableMap<K,V> asMap();
}
