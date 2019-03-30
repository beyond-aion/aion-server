package com.aionemu.commons.configuration.transformers;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * This class is here just for writing less "ifs" in the code. Does nothing
 * 
 * @author SoulKeeper
 */
public class StringTransformer extends PropertyTransformer<String> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final StringTransformer SHARED_INSTANCE = new StringTransformer();

	@Override
	protected String parseObject(String value, TransformationTypeInfo typeInfo) {
		return value;
	}
}
