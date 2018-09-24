package com.navigo3.dryapi.core.util;

public interface Consumer4<T, U, V, W> {
    void accept(T t, U u, V v, W w);
}
