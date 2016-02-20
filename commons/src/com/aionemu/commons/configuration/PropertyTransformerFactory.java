package com.aionemu.commons.configuration;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.TimeZone;
import java.util.regex.Pattern;

import com.aionemu.commons.configuration.transformers.ArrayTransformer;
import com.aionemu.commons.configuration.transformers.BooleanTransformer;
import com.aionemu.commons.configuration.transformers.ByteTransformer;
import com.aionemu.commons.configuration.transformers.CharTransformer;
import com.aionemu.commons.configuration.transformers.ClassTransformer;
import com.aionemu.commons.configuration.transformers.CollectionTransformer;
import com.aionemu.commons.configuration.transformers.DoubleTransformer;
import com.aionemu.commons.configuration.transformers.EnumTransformer;
import com.aionemu.commons.configuration.transformers.FileTransformer;
import com.aionemu.commons.configuration.transformers.FloatTransformer;
import com.aionemu.commons.configuration.transformers.InetSocketAddressTransformer;
import com.aionemu.commons.configuration.transformers.IntegerTransformer;
import com.aionemu.commons.configuration.transformers.LongTransformer;
import com.aionemu.commons.configuration.transformers.PatternTransformer;
import com.aionemu.commons.configuration.transformers.ShortTransformer;
import com.aionemu.commons.configuration.transformers.StringTransformer;
import com.aionemu.commons.configuration.transformers.TimeZoneTransformer;
import com.aionemu.commons.utils.ClassUtils;

/**
 * This class is responsible for creating property transformers. Uses shared instances to avoid overhead.
 * 
 * @author SoulKeeper
 * @modified Neon
 */
public class PropertyTransformerFactory {

	/**
	 * @param clazzToTransform
	 *          Class that is going to be transformed
	 * @param tc
	 *          {@link PropertyTransformer} class that will be instantiated
	 * @return A shared instance of the {@link PropertyTransformer}
	 * @throws TransformationException
	 *           If can't instantiate {@link PropertyTransformer} (most likely due to not supported class)
	 */
	@SuppressWarnings("rawtypes")
	public static PropertyTransformer getTransformer(Class<?> clazzToTransform) throws TransformationException {

		if (clazzToTransform == Boolean.class || clazzToTransform == Boolean.TYPE)
			return BooleanTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Byte.class || clazzToTransform == Byte.TYPE)
			return ByteTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Character.class || clazzToTransform == Character.TYPE)
			return CharTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Double.class || clazzToTransform == Double.TYPE)
			return DoubleTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Float.class || clazzToTransform == Float.TYPE)
			return FloatTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Integer.class || clazzToTransform == Integer.TYPE)
			return IntegerTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Long.class || clazzToTransform == Long.TYPE)
			return LongTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Short.class || clazzToTransform == Short.TYPE)
			return ShortTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == String.class)
			return StringTransformer.SHARED_INSTANCE;
		else if (clazzToTransform.isEnum())
			return EnumTransformer.SHARED_INSTANCE;
		else if (ClassUtils.isSubclass(clazzToTransform, Collection.class))
			return CollectionTransformer.SHARED_INSTANCE;
		else if (clazzToTransform.isArray())
			return ArrayTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == File.class)
			return FileTransformer.SHARED_INSTANCE;
		else if (ClassUtils.isSubclass(clazzToTransform, InetSocketAddress.class))
			return InetSocketAddressTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Pattern.class)
			return PatternTransformer.SHARED_INSTANCE;
		else if (clazzToTransform == Class.class)
			return ClassTransformer.SHARED_INSTANCE;
		else if (ClassUtils.isSubclass(clazzToTransform, TimeZone.class))
			return TimeZoneTransformer.SHARED_INSTANCE;
		else
			throw new TransformationException("Transformer not found for class " + clazzToTransform.getName());
	}
}
