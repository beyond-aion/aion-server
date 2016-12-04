package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;

import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * This class implements basic boolean transformer.
 * <p/>
 * Boolean can be represented by "true/false" (case doen't matter) or "1/0". In other cases
 * {@link com.aionemu.commons.configuration.TransformationException} is thrown
 * 
 * @author SoulKeeper
 */
public class BooleanTransformer extends PropertyTransformer<Boolean> {

	/**
	 * Shared instance of this transformer, it's thread safe so no need to create multiple instances
	 */
	public static final BooleanTransformer SHARED_INSTANCE = new BooleanTransformer();

	/**
	 * Transforms string to boolean.
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return Boolean object that represents transformed value
	 * @throws TransformationException
	 *           if something goes wrong
	 */
	@Override
	protected Boolean parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		// not using "Boolean.parseBoolean" since it never throws an error (returns false if string is not "true" ignoring case)
		if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
			return true;
		} else if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
			return false;
		} else {
			throw new InvalidParameterException("Value is no boolean expression.");
		}
	}
}
