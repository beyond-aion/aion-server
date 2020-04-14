package com.aionemu.commons.configuration.transformers;

import com.aionemu.commons.configuration.TransformationTypeInfo;

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

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Enum<?> parseObject(String value, TransformationTypeInfo typeInfo) {
		return value.isEmpty() ? null : Enum.valueOf((Class<? extends Enum>) typeInfo.getType(), value);
	}
}
