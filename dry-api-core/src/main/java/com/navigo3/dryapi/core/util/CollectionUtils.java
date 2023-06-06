package com.navigo3.dryapi.core.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class CollectionUtils {

	public static <T, U> Stream<U> mapWithIndex(Stream<T> stream, BiFunction<T, Integer, U> block) {
		AtomicInteger i = new AtomicInteger(0);

		return stream.map(item -> block.apply(item, i.getAndIncrement()));
	}

	public static <T> void eachWithIndex(Collection<T> coll, BiConsumer<T, Integer> block) {
		int i = 0;

		Iterator<T> iter = coll.iterator();

		while (iter.hasNext()) {
			block.accept(iter.next(), i++);
		}
	}
}
