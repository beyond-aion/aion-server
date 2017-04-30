package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.PropertyTransformerFactory;

/**
 * Returns an <code>Array</code> containing the comma separated items.<br>
 * Currently only 1-dimensional arrays are supported.
 * 
 * @author Neon
 */
public class ArrayTransformer extends CommaSeparatedValueTransformer<Object[]> {

	public static final ArrayTransformer SHARED_INSTANCE = new ArrayTransformer();

	@Override
	protected Object[] parseObject(List<String> values, Field field, Type... genericTypeArgs) throws Exception {
		Class<?> type = field.getType().getComponentType();
		if (type.isArray())
			throw new UnsupportedOperationException("Multidimensional arrays are not implemented.");

		if (values.isEmpty() || values.get(0).equals(Property.DEFAULT_VALUE))
			return (Object[]) Array.newInstance(type, 0); // return empty

		PropertyTransformer<?> pt = PropertyTransformerFactory.getTransformer(type);
		Object[] array = (Object[]) Array.newInstance(type, values.size());
		for (int i = 0; i < values.size(); i++)
			array[i] = pt.transform(values.get(i), field);

		return array;
	}
}
