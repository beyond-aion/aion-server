package com.aionemu.commons.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Neon
 */
public class ConsoleUtil {

	public static void printSection(String s) {
		System.out.println();
		System.out.print(StringUtils.center("[ " + s + " ]", 80, "="));
	}

	public static void printProgressBarHeader(int size) {
		StringBuilder header = new StringBuilder("0%[");
		for (int i = 0; i < size; i++) {
			header.append("-");
		}
		header.append("]100%");
		System.out.println(header);
		System.out.print("   ");
	}

	public static void printCurrentProgress() {
		System.out.print("+");
	}

	public static void printEndProgress() {
		System.out.print(" Done. \n");
	}
}
