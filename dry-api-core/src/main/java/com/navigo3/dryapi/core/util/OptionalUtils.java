package com.navigo3.dryapi.core.util;

import java.util.Optional;
import java.util.function.Consumer;

public class OptionalUtils {
	public static <T> void ifPresentOrElse(Optional<T> opt, Consumer<T> onPresent, Runnable onMissing) {
		if (opt.isPresent()) {
			onPresent.accept(opt.get());
		} else {
			onMissing.run();
		}
	}
}
