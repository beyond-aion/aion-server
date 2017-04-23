package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.PropertyTransformerFactory;

/**
 * Returns an <code>Array</code> containing the comma separated items.<br>
 * Currently only 1-dimensional arrays are supported.
 * 
 * @author Neon
 */
public class ArrayTransformer extends PropertyTransformer<Object[]> {

	public static final ArrayTransformer SHARED_INSTANCE = new ArrayTransformer();

	@Override
	protected Object[] parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		Class<?> type = field.getType().getComponentType();
		if (type.isArray())
			throw new IllegalArgumentException("Multidimensional arrays are not implemented.");

		if (value.isEmpty() || value.equals(Property.DEFAULT_VALUE))
			return (Object[]) Array.newInstance(type, 0); // return empty

		PropertyTransformer<?> pt = PropertyTransformerFactory.getTransformer(type);
		String[] rawValues = value.split(" *, *");
		Object[] array = (Object[]) Array.newInstance(type, rawValues.length);
		for (int i = 0; i < rawValues.length; i++)
			array[i] = pt.transform(rawValues[i], field);

		return array;
	}
}
