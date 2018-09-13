package com.navigo3.dryapi.core.util;

public interface Function4<T, U, V, W, X> {
    X apply(T t, U u, V v, W w);
}