package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Modifier;
import java.util.*;

import com.aionemu.commons.configuration.PropertyTransformerFactory;
import com.aionemu.commons.configuration.TransformationTypeInfo;

/**
 * Creates a <code>Collection</code> containing the comma separated items.<br>
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
	protected Collection<?> parseObject(List<String> values, TransformationTypeInfo typeInfo) throws Exception {
		Collection<Object> collection;
		if (typeInfo.getType().isInterface() || Modifier.isAbstract(typeInfo.getType().getModifiers())) {
			if (typeInfo.getType() == Collection.class)
				throw new UnsupportedOperationException("Collection type (subclass) must be specified.");
			else if (typeInfo.getType() == List.class)
				collection = new ArrayList<>();
			else if (typeInfo.getType() == Set.class)
				collection = new HashSet<>();
			else
				throw new UnsupportedOperationException("No default implementation for " + typeInfo.getType() + ", non abstract/interface class must be declared.");
		} else {
			collection = (Collection<Object>) typeInfo.getType().getDeclaredConstructor().newInstance();
		}

		if (!values.isEmpty()) {
			TransformationTypeInfo innerType = typeInfo.getGenericType(0);
			PropertyTransformer<?> pt = PropertyTransformerFactory.getTransformer(innerType.getType());
			for (String val : values)
				collection.add(pt.transform(val, innerType));
		}

		return collection;
	}
}
