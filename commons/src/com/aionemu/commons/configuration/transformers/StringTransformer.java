package com.aionemu.commons.configuration.transformers;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * This class is here just for writing less "ifs" in the code. Does nothing
 * 
 * @author SoulKeeper
 */
public class StringTransformer extends PropertyTransformer<String> {

	@Override
	public boolean matches(Class<?> targetType) {
		return targetType == String.class;
	}

	@Override
	protected String parseObject(String value, TransformationTypeInfo typeInfo) {
		return value;
	}
}
