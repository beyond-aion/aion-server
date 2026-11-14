package com.aionemu.commons.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.pattern.color.GrayCompositeConverter;

public class RealGrayCompositeConverter<E> extends GrayCompositeConverter<E> {

	public static final String GRAY_FG = "38;5;242";

	@Override
	protected String getForegroundColorCode(Object event) {
		return GRAY_FG;
	}

  /**
   * Replaces Logback's default GrayCompositeConverter with this one. Logback's uses BOLD + BLACK_FG, which renders black text in IntelliJ's console.
   */
	public static void register() {
		PatternLayout.DEFAULT_CONVERTER_SUPPLIER_MAP.put("gray", RealGrayCompositeConverter::new);
	}
}
