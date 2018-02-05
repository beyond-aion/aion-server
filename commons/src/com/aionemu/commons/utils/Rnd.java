package com.aionemu.commons.utils;

import java.util.List;

/**
 * @author Balancer, Neon
 */
public final class Rnd {

	private static final MTRandom rnd = new MTRandom();

	/**
	 * To compare this chance with a success rate, evaluate "{@code if (chance() < success rate)}" to determine a success or, alternatively
	 * "{@code if (chance() >= success rate)}" to determine a fail. This ensures that a success rate of 0 (0%) will always fail, and a success rate of
	 * 100.0 (100%) always succeeds.
	 * 
	 * @return A random chance between 0.0f (inclusive) and 100.0f (exclusive)
	 */
	public static float chance() {
		return nextFloat() * 100;
	}

	/**
	 * @return Random number from 0.0f (inclusive) to 1.0f (exclusive)
	 * @see MTRandom#nextFloat()
	 */
	public static float get() {
		return nextFloat();
	}

	/**
	 * Gets a random number from 0 (inclusive) to n (exclusive)
	 * 
	 * @param n
	 * @return A number between 0 and n-1
	 */
	public static int get(int n) {
		return (int) Math.floor(rnd.nextFloat() * n);
	}

	/**
	 * Gets a random number from min (inclusive) to max (inclusive)
	 * 
	 * @param min
	 * @param max
	 * @return A number between min and max
	 */
	public static int get(int min, int max) {
		return min + (int) Math.floor(rnd.nextFloat() * (max - min + 1));
	}

	/**
	 * Gets a random element from the given list, null if it's empty
	 * 
	 * @param list
	 * @return Random element
	 */
	public static <T> T get(List<T> list) {
		return list.isEmpty() ? null : list.size() == 1 ? list.get(0) : list.get(get(list.size()));
	}

	/**
	 * Gets a random element from the given array, null if it's empty
	 * 
	 * @param array
	 * @return Random element
	 */
	public static <T> T get(T[] array) {
		return array.length == 0 ? null : array.length == 1 ? array[0] : array[get(array.length)];
	}

	/**
	 * Gets a random element from the given primitive int array (must not be empty)
	 * 
	 * @param array
	 * @return Random element
	 */
	public static int get(int[] array) {
		if (array.length == 0)
			throw new IllegalArgumentException("Cannot get random int from an empty array.");
		return array.length == 1 ? array[0] : array[get(array.length)];
	}

	/**
	 * @see MTRandom#nextBytes(byte[])
	 */
	public static void nextBytes(byte[] bytes) {
		rnd.nextBytes(bytes);
	}

	/**
	 * @see MTRandom#nextInt()
	 */
	public static int nextInt() {
		return rnd.nextInt();
	}

	/**
	 * @see MTRandom#nextFloat()
	 */
	public static float nextFloat() {
		return rnd.nextFloat();
	}

	/**
	 * @see MTRandom#nextDouble()
	 */
	public static double nextDouble() {
		return rnd.nextDouble();
	}

	/**
	 * @see MTRandom#nextGaussian()
	 */
	public static double nextGaussian() {
		return rnd.nextGaussian();
	}

	/**
	 * @see MTRandom#nextBoolean()
	 */
	public static boolean nextBoolean() {
		return rnd.nextBoolean();
	}
}
