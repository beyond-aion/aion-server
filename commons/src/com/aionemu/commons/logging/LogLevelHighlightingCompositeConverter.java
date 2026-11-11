package com.aionemu.commons.logging;

import static ch.qos.logback.core.pattern.color.ANSIConstants.YELLOW_FG;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class LogLevelHighlightingCompositeConverter extends HighlightingCompositeConverter {

	private static final String DARK_GRAY_FG = "90";

	@Override
	protected String getForegroundColorCode(ILoggingEvent event) {
		int levelInt = event.getLevel().toInt();
		if (levelInt <= Level.DEBUG_INT)
			return DARK_GRAY_FG;
		if (levelInt == Level.WARN_INT)
			return YELLOW_FG;
		return super.getForegroundColorCode(event);
	}

	/**
   * Replaces Logback's default HighlightingCompositeConverter with this one. Logback's uses RED_FG for WARN_INT and doesn't have trace/debug colors.
   */
	public static void register() {
		PatternLayout.DEFAULT_CONVERTER_SUPPLIER_MAP.put("highlight", LogLevelHighlightingCompositeConverter::new);
	}
}
