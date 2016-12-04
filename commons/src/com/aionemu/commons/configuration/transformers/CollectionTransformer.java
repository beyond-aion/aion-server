package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.activation.UnsupportedDataTypeException;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.PropertyTransformerFactory;

import javolution.util.FastSet;
import javolution.util.FastTable;

/**
 * Returns a <code>Collection</code> containing the comma separated items.<br>
 * If the declared class is an interface, the following implementations will be used:
 * <ul>
 * <li>{@link List} as {@link FastTable}</li>
 * <li>{@link Set} as {@link FastSet}</li>
 * </ul>
 * <p/>
 * Normal classes will be invoked via the reflective #newInstance() method.
 * Trying to use not implemented interfaces/abstract classes will throw an Exception.
 * If the input is empty, an empty collection will be returned. Output is never null.
 * 
 * @author Neon
 */
public class CollectionTransformer extends PropertyTransformer<Collection<?>> {

	public static final CollectionTransformer SHARED_INSTANCE = new CollectionTransformer();

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<?> parseObject(String value, Field field, Type... genericTypeArgs) throws Exception {
		Class<?> type = field.getType();

		Collection<Object> collection;
		if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
			if (type == Collection.class)
				throw new UnsupportedDataTypeException("Collection type (subclass) must be specified.");
			else if (type == List.class)
				collection = new FastTable<>();
			else if (type == Set.class)
				collection = new FastSet<>();
			else
				throw new UnsupportedDataTypeException("No default implementation for " + type + ", non abstract/interface class must be declared.");
		} else {
			collection = (Collection<Object>) type.newInstance();
		}

		if (value.isEmpty() || value.equals(Property.DEFAULT_VALUE))
			return collection; // return empty

		Type genericType;
		if (genericTypeArgs.length > 0) // <..., ..., ...>
			genericType = genericTypeArgs[0]; // <...>
		else
			throw new UnsupportedDataTypeException("Raw collections are not supported.");

		Type[] innerGenericType = {};
		if (genericType instanceof WildcardType) // <... extends Object>
			genericType = ((WildcardType) genericType).getUpperBounds()[0]; // Object
		else if (genericType instanceof ParameterizedType) { // <Object<...>>
			innerGenericType = ((ParameterizedType) genericType).getActualTypeArguments(); // <...>
			genericType = ((ParameterizedType) genericType).getRawType(); // Object
		}

		if (!(genericType instanceof Class))
			throw new UnsupportedDataTypeException("<" + genericType.getTypeName() + "> must be a valid class.");

		PropertyTransformer<?> pt = PropertyTransformerFactory.getTransformer((Class<?>) genericType);

		Objects.requireNonNull(pt, "Property transformer for " + genericType + " is not implemented.");
		if (pt instanceof CollectionTransformer)
			throw new UnsupportedDataTypeException("Nested collections are not implemented."); // needs class argument in transform()

		for (String val : value.split(" *, *"))
			collection.add(pt.transform(val, field, innerGenericType));

		return collection;
	}
}
