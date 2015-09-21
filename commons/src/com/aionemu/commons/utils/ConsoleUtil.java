package com.aionemu.commons.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Neon
 */
public class ConsoleUtil {

	private static final int MAX_PROGRESS = 68;

	private int position = 0;
	private int currentProgress = 0;
	private double maxProgress = 0;

	public static ConsoleUtil newInstance() {
		return new ConsoleUtil();
	}

	public static void printSection(String s) {
		System.out.println("\n " + StringUtils.center(" [ " + s + " ] ", 78, "="));
	}

	public void printProgressBar(int size) {
		maxProgress = size;
		if (size > MAX_PROGRESS)
			size = MAX_PROGRESS;
		StringBuilder header = new StringBuilder(" 0% [");
		for (int i = 0; i < size; i++) {
			header.append("·");
		}
		header.append("] 100%");
		System.out.print(header + "\r 0% [");
	}

	public void printCurrentProgress() {
		double pos = ((++currentProgress / maxProgress) * (maxProgress < MAX_PROGRESS ? maxProgress : MAX_PROGRESS));
		if (position < pos || currentProgress == maxProgress) {
			for (;position < pos; position++)
				System.out.print("#");
			if (currentProgress == maxProgress)
				System.out.println("] 100%");
		} else if (currentProgress < maxProgress && maxProgress > MAX_PROGRESS*2) {
			switch (currentProgress % 3) {
				case 0:
					System.out.print("/\b");
					break;
				case 1:
					System.out.print("-\b");
					break;
				case 2:
					System.out.print("\\\b");
					break;
			}
		}
	}
}
