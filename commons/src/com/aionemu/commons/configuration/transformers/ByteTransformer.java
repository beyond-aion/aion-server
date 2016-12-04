package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Transforms String to Byte. String can be in decimal or hex format.
 * 
 * @author SoulKeeper
 */
public class ByteTransformer extends PropertyTransformer<Byte> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final ByteTransformer SHARED_INSTANCE = new ByteTransformer();

	/**
	 * Transforms string to byte
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return Byte object that represents value
	 * @throws TransformationException
	 *           if something went wrong
	 */
	@Override
	protected Byte parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		return Byte.decode(value);
	}
}
