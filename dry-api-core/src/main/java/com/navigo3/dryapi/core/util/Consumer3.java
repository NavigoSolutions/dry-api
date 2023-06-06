package com.navigo3.dryapi.core.util;

public interface Consumer3<T, U, V> {
	void accept(T t, U u, V v);
}
