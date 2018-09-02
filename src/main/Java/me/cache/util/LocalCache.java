package me.cache.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import me.cache.util.exception.CacheLoadingException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class LocalCache<K, V> implements ICache<K,V> {
    public final int size;
    public final long expireAfterAccess;
    public final long expireAfterWrite;
    public final long refreshAfterWrite;
    public final TimeUnit timeUnit;
    public final CacheDataLoader<K, V> loader;
    private final Cache<K, V> guavaCache;
    private final boolean asyncRefresh;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    public LocalCache(int size, long expireAfterAccess,
                      long expireAfterWrite, long refreshAfterWrite,
                      TimeUnit timeUnit, CacheDataLoader<K, V> loader, boolean asyncRefresh) {
        this.size = size;
        this.expireAfterAccess = expireAfterAccess;
        this.expireAfterWrite = expireAfterAccess;
        this.refreshAfterWrite = refreshAfterWrite;
        this.timeUnit = timeUnit;
        this.loader = loader;
        this.guavaCache = getGuavaCache();
        this.asyncRefresh = asyncRefresh;
    }

    private Cache getGuavaCache() {
        return CacheBuilder.newBuilder().maximumSize(size)
                .expireAfterAccess(expireAfterAccess, timeUnit)
                .expireAfterWrite(expireAfterWrite, timeUnit)
                .refreshAfterWrite(refreshAfterWrite, timeUnit)
                .build(getGuavaCacheLoader());
    }

    private CacheLoader<K, V> getGuavaCacheLoader() {
        return new CacheLoader<K, V>() {
            @Override
            public V load(K key) throws CacheLoadingException {
                try {
                    return loader.loadData(key);
                } catch (Exception e) {
                    throw new CacheLoadingException(String.format("Error while loading cache for key : [%s]. Exception Message : [%s]", key, e.getLocalizedMessage()), e);
                }
            }

            @Override
            public ListenableFuture<V> reload(final K k, V v) throws CacheLoadingException {
                ListenableFutureTask<V> task = ListenableFutureTask.create(new CallableRefreshTask(k));
                if (asyncRefresh) {
                    task.run();
                } else {
                    executorService.submit(task);
                }
                return task;
            }
        };
    }

    @Override
    public ImmutableMap<K, V> asMap() {
        return ImmutableMap.copyOf(this.guavaCache.asMap());
    }

    public Optional<V> get(K key) throws CacheLoadingException {
        try {
            return Optional.ofNullable(guavaCache.get(key, new CallableRefreshTask(key)));
        } catch (Exception e) {
            throw new CacheLoadingException(e.getLocalizedMessage(), e);
        }
    }

    public void put(K key, V value) {
        guavaCache.put(key, value);
    }

    public void putAll(Map<K,V> map) {
        map.forEach( (key, value)->{
            put(key, value);
        });
    }

    class CallableRefreshTask implements Callable<V> {
        K k;
        CallableRefreshTask(K k) {
            this.k = k;
        }
        @Override
        public V call() throws Exception {
            try {
                return loader.loadData(k);
            } catch (Exception e) {
                throw new CacheLoadingException(String.format("Error while refreshing cache for key : [%s]. Exception Message : [%s]", k, e.getLocalizedMessage()), e);
            }
        }
    }
}
