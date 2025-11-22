package com.aionemu.commons.configuration.transformers;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all transformers available to {@link com.aionemu.commons.configuration.ConfigurableProcessor ConfigurableProcessor}.<br>
 * <br>
 * List of standard supported types (can be expanded via {@link #register(PropertyTransformer)}):
 * <ul>
 * <li>{@link Number}</li>
 * <li>{@link Boolean}</li>
 * <li>{@link Character}</li>
 * <li>{@link String}</li>
 * <li>{@link Class}</li>
 * <li>{@link Enum}</li>
 * <li>Arrays containing elements of supported types</li>
 * <li>{@link java.util.List Lists} and {@link java.util.Set Sets} containing elements of supported types</li>
 * <li>{@link java.io.File}</li>
 * <li>{@link java.net.InetSocketAddress}</li>
 * <li>{@link java.util.regex.Pattern}
 * <li>{@link java.util.TimeZone}</li>
 * <li>{@link java.time.ZoneId}</li>
 * </ul>
 */
public class PropertyTransformers {

	private static final List<PropertyTransformer<?>> transformers = new ArrayList<>();

	static {
		register(new NumberTransformer());
		register(new BooleanTransformer());
		register(new CharTransformer());
		register(new StringTransformer());
		register(new EnumTransformer());
		register(new ArrayTransformer());
		register(new CollectionTransformer());
		register(new FileTransformer());
		register(new InetSocketAddressTransformer());
		register(new PatternTransformer());
		register(new ClassTransformer());
		register(new TimeZoneTransformer());
		register(new ZoneIdTransformer());
	}

	private PropertyTransformers() {
	}

	public static void register(PropertyTransformer<?> propertyTransformer) {
		transformers.add(propertyTransformer);
	}

	public static PropertyTransformer<?> get(Class<?> targetType) {
		for (PropertyTransformer<?> transformer : transformers) {
			if (transformer.matches(targetType))
				return transformer;
		}
		throw new IllegalArgumentException("Transformer for " + targetType.getSimpleName() + " is not registered.");
	}
}
