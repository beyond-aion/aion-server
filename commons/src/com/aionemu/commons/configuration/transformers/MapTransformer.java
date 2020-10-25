package com.aionemu.commons.configuration.transformers;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.aionemu.commons.configuration.PropertyTransformerFactory;
import com.aionemu.commons.configuration.TransformationException;
import com.aionemu.commons.configuration.TransformationTypeInfo;

public class MapTransformer {

	public static Map<?, ?> transform(Map<String, String> values, Class<?> type, Type... genericTypeArgs) throws Exception {
		return transform(values, new TransformationTypeInfo(type, genericTypeArgs));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <K, V> Map<K, V> transform(Map<String, String> values, TransformationTypeInfo typeInfo) throws Exception {
		Class<?> mapType = typeInfo.getType();
		if (!Map.class.isAssignableFrom(mapType))
			throw new IllegalArgumentException(mapType + " is not a Map type");
		Map<K, V> output;
		if (mapType.isInterface() || Modifier.isAbstract(mapType.getModifiers())) {
			if (mapType == Map.class) {
				Class<K> keyType = (Class<K>) typeInfo.getGenericType(0).getType();
				output = keyType.isEnum() ? new EnumMap(keyType) : new HashMap<>();
			} else {
				throw new UnsupportedOperationException("No default implementation for " + mapType + ", non abstract/interface class must be declared.");
			}
		} else {
			output = (Map<K, V>) mapType.getDeclaredConstructor().newInstance();
		}
		transformAndFill(values, output, typeInfo.getGenericType(0), typeInfo.getGenericType(1));
		return output;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <K, V> void transformAndFill(Map<String, String> values, Map<K, V> output, TransformationTypeInfo keyType, TransformationTypeInfo valueType) {
		PropertyTransformer keyTransformer = PropertyTransformerFactory.getTransformer(keyType.getType());
		PropertyTransformer valueTransformer = PropertyTransformerFactory.getTransformer(valueType.getType());
		values.forEach((k, v) -> {
			K key = (K) keyTransformer.transform(k, keyType);
			try {
				V value = (V) valueTransformer.transform(v, valueType);
				output.put(key, value);
			} catch (Exception e) {
				throw new TransformationException("Could not transform property: " + k, e);
			}
		});
	}

}
