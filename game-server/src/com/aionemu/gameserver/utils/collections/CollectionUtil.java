package com.aionemu.gameserver.utils.collections;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

public class CollectionUtil {


	private CollectionUtil() {
	}

	public static <T> void forEach(Iterable<T> iterable, Consumer<T> consumer) {
		forEach(iterable, consumer, null);
	}

	public static <T> void forEach(Iterable<T> iterable, Consumer<T> consumer, BiConsumer<T, Exception> onExceptionAction) {
		iterable.forEach(object -> {
			try {
				consumer.accept(object);
			} catch (Exception e) {
				if (onExceptionAction != null) {
					try {
						onExceptionAction.accept(object, e);
						return;
					} catch (Exception e2) {
						e.addSuppressed(e2);
					}
				}
				LoggerFactory.getLogger(CollectionUtil.class).error("Could not perform operation on " + object, e);
			}
		});
	}
}
