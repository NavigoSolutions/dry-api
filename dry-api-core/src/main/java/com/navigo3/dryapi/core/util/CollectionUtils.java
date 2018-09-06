package com.navigo3.dryapi.core.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class CollectionUtils {
	
	public static <T, U> Stream<U> mapWithIndex(Stream<T> stream, BiFunction<T, Integer, U> block) {
		AtomicInteger i = new AtomicInteger(0);

		return stream.map(item->block.apply(item, i.getAndIncrement()));
	}
	
	public static <T> void eachWithIndex(Stream<T> stream, BiConsumer<T, Integer> block) {
		int i = 0;
		
		while (stream.iterator().hasNext()) {
			block.accept(stream.iterator().next(), i++);
		}
	}
}
