package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.List;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.PropertyTransformerFactory;

import javolution.util.FastTable;

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
			throw new InvalidParameterException("Multidimensional arrays are not implemented.");

		if (value.isEmpty() || value.equals(Property.DEFAULT_VALUE))
			return (Object[]) Array.newInstance(type, 0); // return empty

		PropertyTransformer<?> pt = PropertyTransformerFactory.getTransformer(type);
		List<Object> list = new FastTable<>();
		for (String val : value.split(" *, *"))
			list.add(pt.transform(val, field));

		return list.toArray();
	}
}
