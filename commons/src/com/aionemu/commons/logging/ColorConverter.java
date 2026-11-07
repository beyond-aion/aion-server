package com.aionemu.commons.logging;

import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

/**
 * Colors its content with ANSI codes: {@code %clr(...){faint}} uses the given style, {@code %clr(...)} without a style colors by log level (errors
 * red, warnings yellow, everything else green).
 */
public class ColorConverter extends CompositeConverter<ILoggingEvent> {

	private static final Map<String, String> styles = Map.of("faint", "2", "red", "31", "green", "32", "yellow", "33", "blue", "34", "magenta", "35",
		"cyan", "36");

	@Override
	protected String transform(ILoggingEvent event, String in) {
		String option = getFirstOption();
		String style = option == null ? null : styles.get(option);
		if (style == null)
			style = event.getLevel().toInt() >= Level.ERROR_INT ? "31" : event.getLevel().toInt() >= Level.WARN_INT ? "33" : "32";
		return "\033[" + style + "m" + in + "\033[0;39m";
	}
}
