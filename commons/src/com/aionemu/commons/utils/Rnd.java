package com.aionemu.commons.utils;

/**
 * @author Balancer
 */
public class Rnd {

	private static final MTRandom rnd = new MTRandom();

	/**
	 * @return rnd
	 */
	public static float get() // get random number from 0 to 1
	{
		return rnd.nextFloat();
	}

	/**
	 * Gets a random number from 0(inclusive) to n(exclusive)
	 * 
	 * @param n
	 *          The superior limit (exclusive)
	 * @return A number from 0 to n-1
	 */
	public static int get(int n) {
		return (int) Math.floor(rnd.nextDouble() * n);
	}

	/**
	 * @param min
	 * @param max
	 * @return Integer between min(inclusive) and max(inclusive)
	 */
	public static int get(int min, int max) 
	{
		return min + (int) Math.floor(rnd.nextDouble() * (max - min + 1));
	}

	/**
	 * @param n
	 * @return n
	 */
	public static int nextInt(int n) {
		return (int) Math.floor(rnd.nextDouble() * n);
	}

	/**
	 * @return int
	 */
	public static int nextInt() {
		return rnd.nextInt();
	}

	/**
	 * @return double
	 */
	public static double nextDouble() {
		return rnd.nextDouble();
	}

	/**
	 * @return double
	 */
	public static double nextGaussian() {
		return rnd.nextGaussian();
	}

	/**
	 * @return double
	 */
	public static boolean nextBoolean() {
		return rnd.nextBoolean();
	}
}
