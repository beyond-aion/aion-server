package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.configuration.PropertyTransformer;
import com.aionemu.commons.configuration.PropertyTransformerFactory;

/**
 * Returns a <code>Collection</code> containing the comma separated items.<br>
 * If the declared class is an interface, the following implementations will be used:
 * <ul>
 * <li>{@link List} as {@link ArrayList}</li>
 * <li>{@link Set} as {@link HashSet}</li>
 * </ul>
 * <p/>
 * Normal classes will be invoked via the reflective #newInstance() method.
 * Trying to use not implemented interfaces/abstract classes will throw an Exception.
 * If the input is empty, an empty collection will be returned. Output is never null.
 * 
 * @author Neon
 */
public class CollectionTransformer extends CommaSeparatedValueTransformer<Collection<?>> {

	public static final CollectionTransformer SHARED_INSTANCE = new CollectionTransformer();

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<?> parseObject(List<String> values, Field field, Type... genericTypeArgs) throws Exception {
		Class<?> type = field.getType();

		Collection<Object> collection;
		if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
			if (type == Collection.class)
				throw new UnsupportedOperationException("Collection type (subclass) must be specified.");
			else if (type == List.class)
				collection = new ArrayList<>();
			else if (type == Set.class)
				collection = new HashSet<>();
			else
				throw new UnsupportedOperationException("No default implementation for " + type + ", non abstract/interface class must be declared.");
		} else {
			collection = (Collection<Object>) type.newInstance();
		}

		if (values.isEmpty() || values.get(0).equals(Property.DEFAULT_VALUE))
			return collection; // return empty

		Type genericType;
		if (genericTypeArgs.length > 0) // <..., ..., ...>
			genericType = genericTypeArgs[0]; // <...>
		else
			throw new UnsupportedOperationException("Raw collections are not supported.");

		Type[] innerGenericType = {};
		if (genericType instanceof WildcardType) // <... extends Object>
			genericType = ((WildcardType) genericType).getUpperBounds()[0]; // Object
		else if (genericType instanceof ParameterizedType) { // <Object<...>>
			innerGenericType = ((ParameterizedType) genericType).getActualTypeArguments(); // <...>
			genericType = ((ParameterizedType) genericType).getRawType(); // Object
		}

		if (!(genericType instanceof Class))
			throw new UnsupportedOperationException("<" + genericType.getTypeName() + "> must be a valid class.");

		PropertyTransformer<?> pt = PropertyTransformerFactory.getTransformer((Class<?>) genericType);

		Objects.requireNonNull(pt, "Property transformer for " + genericType + " is not implemented.");
		if (pt instanceof CollectionTransformer)
			throw new UnsupportedOperationException("Nested collections are not implemented."); // needs class argument in transform()

		for (String val : values)
			collection.add(pt.transform(val, field, innerGenericType));

		return collection;
	}
}
