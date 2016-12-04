package com.aionemu.commons.utils;

/**
 * @author Balancer, Neon
 */
public class Rnd {

	private static final MTRandom rnd = new MTRandom();

	/**
	 * @return Random number from 0.0f (inclusive) to 1.0f (exclusive)
	 * @see MTRandom#nextFloat()
	 */
	public static float get() {
		return rnd.nextFloat();
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
