package com.navigo3.dryapi.core.util;

import java.util.function.Consumer;

public class IntegerUtils {
	public static void times(int count, Consumer<Integer> block) {
		Validate.nonNegative(count);
		
		for (int i=0;i<count;++i) {
			block.accept(i);
		}
	}
}
