package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import javax.activation.UnsupportedDataTypeException;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.PropertyTransformerFactory;
import com.aionemu.commons.configuration.TransformationException;

import javolution.util.FastTable;

/**
 * Returns an <code>Array</code> containing the comma separated items.<br>
 * Currently only 1-dimensional arrays are supported.
 * 
 * @author Neon
 */
public class ArrayTransformer implements PropertyTransformer<Object[]> {

	public static final ArrayTransformer SHARED_INSTANCE = new ArrayTransformer();

	@Override
	public Object[] transform(String value, Field field, Type... genericTypeArgs) throws TransformationException {
		try {
			Class<?> type = field.getType().getComponentType();
			if (type.isArray())
				throw new UnsupportedDataTypeException("Multidimensional arrays are not implemented.");

			if (value.isEmpty() || value.equals(Property.DEFAULT_VALUE))
				return (Object[]) Array.newInstance(type, 0); // return empty

			PropertyTransformer<?> pt = PropertyTransformerFactory.getTransformer((Class<?>) type);
			List<Object> list = new FastTable<>();
			for (String val : value.split("[ ]*,[ ]*"))
				list.add(pt.transform(val, field, new Type[] {}));

			return list.toArray();
		} catch (Exception e) {
			throw new TransformationException("Cannot create collection from '" + value + "'", e);
		}
	}
}
