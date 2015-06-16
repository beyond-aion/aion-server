package com.aionemu.commons.utils.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;

/**
 * 
 * @author Oleh_Faizulin
 * 
 * @param <K>
 *            Map Key
 * @param <V>
 *            Map Value
 */
public class GenericMapAdapter<K, V> extends XmlAdapter<GenericMapAdapter.KeyValuePairContainer<K, V>, Map<K, V>> {

	@Override
	public KeyValuePairContainer<K, V> marshal(Map<K, V> v) throws Exception {
		if (v == null) {
			return null;
		}

		KeyValuePairContainer<K, V> result = new KeyValuePairContainer<K, V>();
		for (Map.Entry<K, V> entry : v.entrySet()) {
			result.addElement(entry);
		}
		return result;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public Map<K, V> unmarshal(KeyValuePairContainer<K, V> v) throws Exception {
		Map<K, V> result = new HashMap<K, V>();
		for (KeyValuePair<K, V> kvp : v.getValues()) {
			if (kvp.getMapValue() != null) {
				result.put(kvp.getKey(), (V) kvp.getMapValue());
			} else if (kvp.getCollectionValue() != null) {
				result.put(kvp.getKey(), (V) kvp.getCollectionValue());
			} else {
				result.put(kvp.getKey(), kvp.getValue());
			}
		}
		return result;
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class KeyValuePairContainer<K, V> {

		@XmlElement(name = "mapEntry")
		private List<KeyValuePair<K, V>> values;

		public void addElement(Map.Entry<K, V> entry) {
			if (values == null) {
				values = new ArrayList<KeyValuePair<K, V>>();
			}
			values.add(new KeyValuePair<K, V>(entry));
		}

		public List<KeyValuePair<K, V>> getValues() {
			if (values == null) {
				return Collections.emptyList();
			}
			return values;
		}
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.NONE)
	public static class KeyValuePair<K, V> {
		public KeyValuePair() {

		}

		public KeyValuePair(Map.Entry<K, V> entry) {
			this(entry.getKey(), entry.getValue());
		}

		@SuppressWarnings("rawtypes")
		public KeyValuePair(K key, V value) {
			this.key = key;

			if (value instanceof Collection) {
				this.collectionValue = (Collection) value;
			} else if (value instanceof Map) {
				this.mapValue = (Map) value;
			} else {
				this.value = value;
			}
		}

		@XmlElement
		private K key;

		@XmlElement
		private V value;

		@XmlElement
		@SuppressWarnings("rawtypes")
		private Collection collectionValue;

		@XmlElement
		@SuppressWarnings("rawtypes")
		@XmlJavaTypeAdapter(value = GenericMapAdapter.class)
		private Map mapValue;

		public K getKey() {
			return key;
		}

		public V getValue() {
			return value;
		}

		@SuppressWarnings("rawtypes")
		public Collection getCollectionValue() {
			return collectionValue;
		}

		@SuppressWarnings("rawtypes")
		public Map getMapValue() {
			return mapValue;
		}
	}
}
