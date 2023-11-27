package com.aionemu.commons.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Neon
 */
public class ConsoleUtil {

	public static final int DEFAULT_CONSOLE_WIDTH = 80;

	/**
	 * Prevent instantiation
	 */
	private ConsoleUtil() {
	}

	/**
	 * Initializes and prints a progress bar with the given capacity to the console.<br>
	 * Only one progress bar can be used at a time.
	 * 
	 * @param maxProgress
	 *          The capacity of the progress
	 * @see ConsoleProgressBar#init(int maxProgress)
	 */
	public static void initAndPrintProgressBar(int maxProgress) {
		ConsoleProgressBar.getInstance().init(maxProgress);
	}

	/**
	 * Increases the progress by one and prints it to the progress bar.<br>
	 * Note that {@link #initAndPrintProgressBar(int maxProgress)} must be called prior to calling this method for the first time.
	 * 
	 * @see ConsoleProgressBar#increaseAndPrintProgress()
	 */
	public static void increaseAndPrintProgress() {
		ConsoleProgressBar.getInstance().increaseAndPrintProgress();
	}

	public static void printSection(String s) {
		System.out.println("\n " + StringUtils.center(" [ " + s + " ] ", DEFAULT_CONSOLE_WIDTH - 2, "="));
	}
}
