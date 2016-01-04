package com.aionemu.gameserver.utils.xml;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import javolution.util.FastMap;

/**
 * @author Rolandas
 */
public class CaseInsensitiveEnumAdapter<E extends Enum<E>> extends XmlAdapter<String, E> {

	private Map<String, Enum<E>> annotationValueToEnum = new FastMap<>();
	private Map<Enum<E>, String> enumToAnnotationValue = new FastMap<>();

	public CaseInsensitiveEnumAdapter(Class<E> clazz) {
		try {
			for (E each : clazz.getEnumConstants()) {
				Annotation[] annotations = clazz.getField(each.name()).getAnnotations();
				boolean added = false;
				for (Annotation annotation : annotations) {
					if (annotation instanceof XmlEnumValue) {
						String value = ((XmlEnumValue) annotation).value().toLowerCase();
						if (annotationValueToEnum.containsKey(value))
							throw new AnnotationFormatError("Duplicate annotation '" + value + "' in enum class " + clazz.getCanonicalName());
						annotationValueToEnum.put(value, each);
						enumToAnnotationValue.put(each, value);
						added = true;
					}
				}
				if (!added) {
					annotationValueToEnum.put(each.name().toLowerCase(), each);
					enumToAnnotationValue.put(each, each.name());
				}
			}
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public E unmarshal(String v) throws Exception {
		return (E) annotationValueToEnum.get(v.trim().toLowerCase());
	}

	@Override
	public String marshal(E v) throws Exception {
		return enumToAnnotationValue.get(v);
	}
}
