package com.aionemu.commons.utils;

import java.util.Collection;
import java.util.Map;

public class GenericValidator {

	public static boolean isBlankOrNull(String s) {
		return s == null || s.isEmpty();
	}

	public static boolean isBlankOrNull(Collection<?> c) {
		return c == null || c.isEmpty();
	}

	public static boolean isBlankOrNull(Map<?, ?> m) {
		return m == null || m.isEmpty();
	}

	public static boolean isBlankOrNull(Number n) {
		return n == null || n.doubleValue() == 0;
	}

	public static boolean isBlankOrNull(Object[] a) {
		return a == null || a.length == 0;
	}
}