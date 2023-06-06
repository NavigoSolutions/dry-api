package com.navigo3.dryapi.core.util;

import java.time.Duration;

public class ThreadUtils {
	public static void optimisticSleep(Duration duration) {
		try {
			Thread.sleep(duration.toMillis());
		} catch (InterruptedException e) {
			// ignore
		}
	}
}
