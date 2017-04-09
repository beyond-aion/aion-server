package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.TransformationException;

/**
 * Automatic pattern transformer for RegExp resolving
 * 
 * @author SoulKeeper
 */
public class PatternTransformer extends PropertyTransformer<Pattern> {

	/**
	 * Shared instance of this transformer
	 */
	public static final PatternTransformer SHARED_INSTANCE = new PatternTransformer();

	/**
	 * Transforms String to Pattern object
	 * 
	 * @param value
	 *          value that will be transformed
	 * @param field
	 *          value will be assigned to this field
	 * @return Pattern Object
	 * @throws TransformationException
	 *           if pattern is not valid
	 */
	@Override
	protected Pattern parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		if (value.isEmpty() || value.equals(Property.DEFAULT_VALUE))
			return null;
		else
			return Pattern.compile(value);
	}
}
