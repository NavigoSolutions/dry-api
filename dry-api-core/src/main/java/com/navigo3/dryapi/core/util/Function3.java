package com.navigo3.dryapi.core.util;

public interface Function3<T, U, V, W> {
    W apply(T t, U u, V v);
}
