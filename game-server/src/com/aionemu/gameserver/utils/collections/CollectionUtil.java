package com.aionemu.gameserver.utils.collections;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

public class CollectionUtil {

	private CollectionUtil() {
	}

	public static <T> void forEach(Iterable<T> iterable, Consumer<? super T> consumer) {
		forEach(iterable, consumer, null);
	}

	public static <T> void forEach(Iterable<T> iterable, Consumer<? super T> consumer, Supplier<String> exceptionLogDetailsSupplier) {
		for (T object : iterable) {
			try {
				consumer.accept(object);
			} catch (Exception e) {
				String details = exceptionLogDetailsSupplier == null ? "" : " (" + exceptionLogDetailsSupplier.get() + ")";
				LoggerFactory.getLogger(CollectionUtil.class).error("Could not perform operation on {}{}", object, details, e);
			}
		}
	}
}
