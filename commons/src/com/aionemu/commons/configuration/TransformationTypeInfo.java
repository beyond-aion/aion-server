package com.aionemu.commons.configuration;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Metadata for transformers to help decide which type to instantiate for deserialization.
 * 
 * @author Neon
 */
public class TransformationTypeInfo {

	private final Class<?> type;
	private final Type[] genericTypeArgs;

	public TransformationTypeInfo(Class<?> type, Type[] genericTypeArgs) {
		this.type = type;
		this.genericTypeArgs = genericTypeArgs;
	}

	public Class<?> getType() {
		return type;
	}

	public int getGenericTypeCount() {
		return genericTypeArgs.length;
	}

	public TransformationTypeInfo getGenericType(int index) {
		Type innerType;
		if (index >= genericTypeArgs.length) // <..., ..., ...>
			throw new IndexOutOfBoundsException(type.getSimpleName() + " has not enough generic arguments. Tried to access index " + index + ".");
		innerType = genericTypeArgs[index]; // <...>

		Type[] innerGenericType = {};
		if (innerType instanceof WildcardType) // <... extends Object>
			innerType = ((WildcardType) innerType).getUpperBounds()[0]; // Object
		if (innerType instanceof ParameterizedType) { // <Object<...>>
			innerGenericType = ((ParameterizedType) innerType).getActualTypeArguments(); // <...>
			innerType = ((ParameterizedType) innerType).getRawType(); // Object
		}
		if (!(innerType instanceof Class))
			throw new UnsupportedOperationException("<" + innerType.getTypeName() + "> of " + type.getSimpleName() + " must be a valid class.");
		return new TransformationTypeInfo((Class<?>) innerType, innerGenericType);
	}
}
