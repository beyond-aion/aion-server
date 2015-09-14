package com.aionemu.gameserver.utils;

import java.util.List;

import com.aionemu.commons.utils.MTRandom;

/**
 * @author Alcapwnd
 */
public class RndArray {

	private static final MTRandom rnd = new MTRandom();

	public static float get() {
		return rnd.nextFloat();
	}

	public static int get(int n) {
		return (int) Math.floor(rnd.nextDouble() * n);
	}

	public static int get(int min, int max) {
		return min + (int) Math.floor(rnd.nextDouble() * (max - min + 1));
	}

	public static boolean chance(int chance) {
		return (chance >= 1) && ((chance > 99) || (nextInt(99) + 1 <= chance));
	}

	public static boolean chance(double chance) {
		return nextDouble() <= chance / 100.0D;
	}

	public static <E> E get(E[] list) {
		return list[get(list.length)];
	}

	public static int get(int[] list) {
		return list[get(list.length)];
	}

	public static <E> E get(List<E> list) {
		return list.get(get(list.size()));
	}

	public static int nextInt(int n) {
		return (int) Math.floor(rnd.nextDouble() * n);
	}

	public static int nextInt() {
		return rnd.nextInt();
	}

	public static double nextDouble() {
		return rnd.nextDouble();
	}

	public static double nextGaussian() {
		return rnd.nextGaussian();
	}

	public static boolean nextBoolean() {
		return rnd.nextBoolean();
	}
}
