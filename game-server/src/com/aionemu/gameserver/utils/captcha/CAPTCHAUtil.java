package com.aionemu.gameserver.utils.captcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.slf4j.LoggerFactory;

/**
 * @author Cura
 */
public class CAPTCHAUtil {

	private final static int DEFAULT_WORD_LENGTH = 6;
	private final static String WORD = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";

	private final static int IMAGE_WIDTH = 160;
	private final static int IMAGE_HEIGHT = 80;
	private final static int TEXT_SIZE = 25;
	private final static String FONT_FAMILY_NAME = "Verdana";

	/**
	 * create CAPTCHA
	 * 
	 * @param word
	 * @return byte[]
	 */
	public static ByteBuffer createCAPTCHA(String word) {
		BufferedImage bImg = createImage(word);
		return DDSConverter.convertToDxt1NoTransparency(bImg);
	}

	/**
	 * CAPTCHA image create
	 * 
	 * @param word
	 *          text word
	 * @return BufferedImage
	 */
	private static BufferedImage createImage(String word) {
		BufferedImage bImg;

		try {
			// image create
			bImg = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB_PRE);
			Graphics2D g2 = bImg.createGraphics();

			// set backgroup color
			g2.setColor(Color.BLACK);
			g2.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

			// set font family, color, size, antialiasing
			Font font = new Font(FONT_FAMILY_NAME, Font.BOLD, TEXT_SIZE);
			g2.setFont(font);
			g2.setColor(Color.WHITE);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			// word drawing
			char[] chars = word.toCharArray();
			int x = 10;
			int y = IMAGE_HEIGHT / 2 + TEXT_SIZE / 2;

			for (int i = 0; i < chars.length; i++) {
				char ch = chars[i];
				g2.drawString(String.valueOf(ch), x + font.getSize() * i, y + (int) Math.pow(-1, i) * (TEXT_SIZE / 6));
			}

			// resource dispose
			g2.dispose();
		} catch (Exception e) {
			LoggerFactory.getLogger(CAPTCHAUtil.class).error("", e);
			bImg = null;
		}

		return bImg;
	}

	/**
	 * @return String random word
	 */
	public static String getRandomWord() {
		return randomWord(DEFAULT_WORD_LENGTH);
	}

	/**
	 * @return CAPTCHA word
	 */
	private static String randomWord(int wordLength) {
		StringBuilder word = new StringBuilder();

		for (int i = 0; i < wordLength; i++) {
			int index = Math.abs((int) (Math.random() * WORD.length()));
			char ch = WORD.charAt(index);
			word.append(ch);
		}

		return word.toString();
	}
}
