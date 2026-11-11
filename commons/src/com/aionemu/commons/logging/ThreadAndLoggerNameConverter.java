package com.aionemu.commons.logging;

import static ch.qos.logback.core.pattern.color.ANSIConstants.*;
import static com.aionemu.commons.logging.RealGrayCompositeConverter.GRAY_FG;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class ThreadAndLoggerNameConverter extends ClassicConverter {

	private int width = 50;

	@Override
	public void start() {
		String option = getFirstOption();
		if (option != null) {
			try {
				width = Integer.parseInt(option);
			} catch (NumberFormatException _) {
				addError("Invalid width \"" + option + "\", using default value: " + width);
			}
		}
		super.start();
	}

	@Override
	public String convert(ILoggingEvent event) {
		String threadName = replaceStart(event.getThreadName(), "ForkJoinPool.commonPool-", "FJ-pool-");
		String thread = "[" + threadName + "]";
		String space = " ";
		int loggerMaxLen = Math.max(1, width - thread.length() - space.length());
		String loggerName = event.getLoggerName();
		String logger = loggerName.isEmpty() ? "" : new TargetLengthBasedClassNameAbbreviator(loggerMaxLen).abbreviate(loggerName);
		int threadAndLoggerLength = thread.length() + logger.length();
		if (threadAndLoggerLength >= width) {
			thread = shortenThread(threadName, width - logger.length() - (logger.isEmpty() ? 0 : space.length()));
			threadAndLoggerLength = thread.length() + logger.length();
		}
		int paddingLength = Math.max(logger.isEmpty() ? 0 : 1, width - threadAndLoggerLength);
		String padding = space.repeat(paddingLength);
		return format(thread, padding, logger);
	}

	private static String replaceStart(String threadName, String start, String replacement) {
		if (threadName.startsWith(start))
			return replacement + threadName.substring(start.length());
		return threadName;
	}

	private static String shortenThread(String threadName, int length) {
		threadName = replaceStart(threadName, "ReadWrite", "RW");
		threadName = replaceStart(threadName, "PacketProcessor", "PP");
		length -= 2; // enclosing brackets
		length = Math.max(4, length); // shorter would be unhelpful
		return "[" + truncateLeft(threadName, length) + "]";
	}

	private static String truncateLeft(String s, int maxLength) {
		if (s.length() <= maxLength)
			return s;
		int beginIndex = s.length() - maxLength + 1;
		return beginIndex <= 0 || beginIndex >= s.length() ? "…" : "…" + s.substring(beginIndex);
	}

	private static String format(String thread, String padding, String logger) {
		String formatted = ESC_START + GRAY_FG + ESC_END + thread + padding;
		if (!logger.isEmpty())
			formatted += ESC_START + CYAN_FG + ESC_END + logger;
		formatted += ESC_START + "0" + ESC_END;
		return formatted;
	}
}
