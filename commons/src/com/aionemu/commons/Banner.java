package com.aionemu.commons;

import static ch.qos.logback.core.pattern.color.ANSIConstants.*;

import java.util.List;

public class Banner {

	private static final String BANNER = """
		⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠠⢤⣤⣤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
		⠈⠉⢻⣿⣿⠿⠿⠿⣶⣦⠀⣀⣀⣀⣀⣀⣀⣀⣀⣀⢠⣀⠀⠀⠀⠀⠀⠀⣀⣀⣀⡀⢀⢀⣀⣀⣀⣀⡀⠀⠀⠀⠀⠀⠀⣀⡀⠀⠀⠀⠀⠀⡀⠀⢀⣀⣀⣀⣀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢈⣿⣿⣷⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣾⣇⠀
		⠀⠀⢸⣿⣿⠀⠀⠀⢘⣿⡆⠀⢹⣿⣿⠉⠉⠉⠉⢻⠀⢻⣿⡉⠒⠀⠀⣠⣿⠟⢀⢊⣾⡿⠛⠉⠉⠛⢿⣷⣄⠉⠉⣿⣿⣟⠀⠀⠀⠀⢀⣴⣿⡀⠀⣿⣿⡟⠛⠻⢿⣷⣤⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⡿⣿⣿⣷⠀⠀⠀⠲⣶⣶⡶⠒⠀⠀⡀⢀⣀⣀⣀⣀⣀⠀⠀⠀⠒⢲⣶⣶⡏⠀⠀⠀⢀⠜⢿⣿⣿⠀
		⠀⠀⣸⣿⣿⣤⣤⣤⡾⠟⠀⠀⢸⣿⣿⠀⠀⠀⠀⠀⠀⠀⢝⣿⣆⠀⣰⣿⠋⠀⣼⣿⡟⠀⠀⣴⣦⠀⠀⢻⣿⣇⠀⣿⣿⣿⣆⠀⠀⡠⠊⢻⣿⡇⠀⣿⣿⡇⠀⠀⠀⠘⣿⣿⣆⠀⠀⠀⠀⠀⠀⠀⠀⣰⣿⠃⠘⣿⣿⣧⠀⠀⠀⣿⣿⡇⠀⡠⣡⣾⡿⠛⠉⠉⠙⠻⣿⣶⣄⠀⢸⣿⣿⣧⡀⠀⠀⠀⠀⢸⣿⣿⠀
		⠀⠀⢹⣿⣿⠛⠛⠛⠻⣷⣄⠀⢸⣿⣿⣤⣤⣤⣾⠀⠀⠀⠀⠙⢿⣿⡟⠁⠀⠀⣿⣿⡇⠀⢸⣿⣿⡇⠀⢸⣿⣿⠀⣿⣿⡇⠉⠳⣄⠀⠀⢸⣿⡇⠀⣿⣿⡇⠀⠀⠀⠀⢸⣿⣿⠀⠀⠀⠀⠀⠀⢀⣴⣿⣇⣀⣀⣸⣿⣿⣆⠀⠀⣿⣿⡇⢰⣿⣿⠏⠀⠀⣠⣦⡀⠀⠘⣿⣿⣆⢸⣿⣿⠙⠻⣦⡀⠀⠀⢸⣿⣿⠀
		⠀⠀⢸⣿⣿⠀⠀⠀⠀⢹⣿⡇⢸⣿⣿⠀⠀⠀⠙⠀⠀⠀⠀⠀⢸⣿⣧⠀⠀⠀⢿⣿⡇⠀⠸⣿⣿⠇⠀⢸⣿⡿⠀⣿⣿⡇⠀⠀⠈⠻⣦⣸⣿⣧⠀⣿⣿⡇⠀⠀⠀⠀⣸⣿⣿⠀⠀⠀⠀⠀⠀⣸⣿⡏⠀⠀⠀⠀⠹⣿⣿⣆⠀⣿⣿⡇⣿⣿⣿⠀⠀⢠⣿⣿⣧⠀⠀⢹⣿⣿⢸⣿⣿⠀⠀⠀⠹⣶⣄⣸⣿⣿⡄
		⠀⠀⢸⣿⣿⠀⠀⠀⢀⣾⣿⠃⢸⣿⣿⠀⠀⠀⠀⠀⡀⠀⠀⠀⢸⣿⣿⠀⠀⠀⠘⢻⣿⡄⠀⠈⠁⠀⢠⣿⡿⠃⠀⣿⣿⡇⠀⠀⠀⠀⠈⢻⣿⡟⠀⣿⣿⡇⠀⠀⢀⣴⣿⡿⠃⠀⠀⠀⠀⠀⣰⣿⡟⠀⠀⠀⠀⠀⠀⢻⣿⣿⣆⣿⣿⡇⢹⣿⣿⡀⠀⠈⣿⣿⠏⠀⠀⣾⣿⡿⢸⣿⣿⠀⠀⠀⠀⠀⠙⢿⣿⣿⠃
		⠀⠠⠾⠿⠿⠿⠿⠿⠿⠋⠊⠠⠾⠿⠿⠶⠶⠶⠶⠾⠇⠀⠀⠠⠾⠿⠿⠄⠀⠀⠀⠀⠉⠛⠶⠄⠠⠖⠿⠋⠁⠀⠤⠿⠿⠧⠀⠀⠀⠀⠀⠀⠙⠁⠠⠿⠿⠷⠶⠿⠛⠋⠊⠀⠀⠀⠀⠀⠒⠚⠛⠛⠓⠀⠀⠀⠀⠀⠀⠚⠛⠛⣻⣿⣿⣷⡀⠙⢿⣷⡄⠀⠈⠉⠀⢀⣼⣿⡿⣁⣼⣿⣿⣆⠀⠀⠀⠀⠀⠀⠻⡏⠀
		⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠛⠓⠂⠀⠲⠟⠛⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
		""";
	private static final String BANNER_COLOR = ESC_START + "38;5;81;40" + ESC_END;
	private static final String BORDER_LEFT = """
		 ▟
		▚▟
		▗
		▜
		▟
		▚
		▛
		▖▜
		 ▞
		""";
	private static final String BORDER_RIGHT = """
		 ▚
		 ▛
		  ▙
		  ▝
		 ▛▚
		  ▟
		  ▛
		 ▞
		▛
		""";

	private Banner() {
	}

	public static void print() {
		IO.print(createBanner(3, 1));
	}

	private static String createBanner(int leftBorderMinWidth, int rightBorderPadding) {
		List<String> leftBorderLines = BORDER_LEFT.lines().toList();
		List<String> bannerLines = BANNER.lines().toList();
		List<String> rightBorderLines = BORDER_RIGHT.lines().toList();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bannerLines.size(); i++) {
			String leftBorderLine = leftBorderLines.get(i);
			String rightBorderLine = rightBorderLines.get(i).trim();
			int rightOffset = rightBorderLines.get(i).length() - rightBorderLine.length();
			String leftPadding = " ".repeat(Math.max(0, leftBorderMinWidth - leftBorderLine.length()));
			String rightPadding = "⠀".repeat(Math.max(0, rightBorderPadding + rightOffset));
			sb.append(ESC_START).append(BLACK_FG).append(ESC_END)
				.append(leftBorderLine)
				.append(BANNER_COLOR)
				.append(leftPadding)
				.append(fixBrailleSpacing(bannerLines.get(i), BANNER_COLOR))
				.append(rightPadding)
				.append(ESC_START).append(RESET).append(BLACK_FG).append(ESC_END)
				.append(rightBorderLine)
				.append(ESC_START).append(RESET).append(DEFAULT_FG).append(ESC_END)
				.append(System.lineSeparator());
		}
		return sb.toString();
	}

	private static String fixBrailleSpacing(String text, String color) {
		// some monospaced fonts render braille characters wider than others, including the blank one
		return text.replace("⠀", ESC_START + BLACK_FG + ESC_END + "⠐" + color);
	}
}