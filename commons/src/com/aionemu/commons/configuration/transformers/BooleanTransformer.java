package com.aionemu.commons.configuration.transformers;

import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * This class implements basic boolean transformer.
 * <p/>
 * Boolean can be represented by true / false (case doesn't matter) or 1 / 0.
 * 
 * @author SoulKeeper
 */
public class BooleanTransformer extends PropertyTransformer<Boolean> {

	/**
	 * Shared instance of this transformer. It's thread-safe so no need of multiple instances
	 */
	public static final BooleanTransformer SHARED_INSTANCE = new BooleanTransformer();

	@Override
	protected Boolean parseObject(String value, TransformationTypeInfo typeInfo) {
		// not using "Boolean.parseBoolean" since it never throws an error (returns false if string is not "true" ignoring case)
		if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
			return true;
		} else if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
			return false;
		} else {
			throw new IllegalArgumentException("Only true, false, 1 and 0 are allowed.");
		}
	}
}
