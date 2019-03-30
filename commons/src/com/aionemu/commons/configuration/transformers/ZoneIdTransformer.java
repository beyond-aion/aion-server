package com.aionemu.commons.configuration.transformers;

import java.time.ZoneId;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * @author Neon
 */
public class ZoneIdTransformer extends PropertyTransformer<ZoneId> {

	/**
	 * Shared instance of this transformer, it's thread safe so no need to create multiple instances
	 */
	public static final ZoneIdTransformer SHARED_INSTANCE = new ZoneIdTransformer();

	@Override
	protected ZoneId parseObject(String value, TransformationTypeInfo typeInfo) {
		return value.isEmpty() ? ZoneId.systemDefault() : ZoneId.of(value);
	}
}
