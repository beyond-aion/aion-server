package com.aionemu.loginserver.utils;

/**
 * @author lord_rex
 */
public class Util {

	/**
	 * @param s
	 */
	public static void printSection(String s) {
		s = "-[ " + s + " ]";

		while (s.length() < 79)
			s = "=" + s;

		System.out.println(s);
	}
}
