package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Transforms enum string representation to enum. String must match case definition of enum, for instance:
 * 
 * <pre>
 * enum{
 *  FILE,
 *  URL
 * }
 * </pre>
 * 
 * will be parsed with string "FILE" but not "file".
 * 
 * @author SoulKeeper
 */
public class EnumTransformer extends PropertyTransformer<Enum<?>> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final EnumTransformer SHARED_INSTANCE = new EnumTransformer();

	/**
	 * Transforms string to enum
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return Enum object representing the value
	 * @throws TransformationException
	 *           if something went wrong
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Enum<?> parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		@SuppressWarnings("rawtypes")
		Class<? extends Enum> clazz = (Class<? extends Enum>) field.getType();
		return value.isEmpty() || value.equals(Property.DEFAULT_VALUE) ? null : Enum.valueOf(clazz, value);
	}
}
