package me.cache.util.exception;

public class CacheLoadingException extends Exception {
    public CacheLoadingException(String msg, Throwable th) {
        super(msg, th);
    }

    public CacheLoadingException(String msg) {
        super(msg);
    }
}
