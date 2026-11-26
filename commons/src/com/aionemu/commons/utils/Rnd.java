package com.aionemu.commons.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.slf4j.LoggerFactory;

/**
 * @author Balancer, Neon
 */
public final class Rnd {

	private Rnd() {
	}

	/**
	 * To compare this chance with a success rate, evaluate "{@code if (chance() < success rate)}" to determine a success or, alternatively
	 * "{@code if (chance() >= success rate)}" to determine a fail. This ensures that a success rate of 0 (0%) will always fail, and a success rate of
	 * 100.0 (100%) always succeeds.
	 * 
	 * @return A random chance between 0.0f (inclusive) and 100.0f (exclusive)
	 */
	public static float chance() {
		return nextFloat(100f);
	}

	/**
	 * @return A random number between minInclusive and maxInclusive
	 */
	public static int get(int minInclusive, int maxInclusive) {
		if (maxInclusive < minInclusive) {
			LoggerFactory.getLogger(Rnd.class).warn("maxInclusive ({}) < minInclusive ({})", maxInclusive, minInclusive, new IllegalArgumentException());
			maxInclusive = minInclusive;
		}
		if (minInclusive == maxInclusive)
			return minInclusive;
		return maxInclusive < Integer.MAX_VALUE ? nextInt(minInclusive, maxInclusive + 1) : (int) nextLong(minInclusive, maxInclusive + 1L);
	}

	/**
	 * @return A random element from the given list, null if it's empty
	 */
	public static <T> T get(List<T> list) {
		return list.isEmpty() ? null : list.size() == 1 ? list.getFirst() : list.get(nextInt(list.size()));
	}

	/**
	 * @return A random element from the given array, null if it's empty
	 */
	public static <T> T get(T[] array) {
		return array.length == 0 ? null : array.length == 1 ? array[0] : array[nextInt(array.length)];
	}

	/**
	 * @return A random element from the given primitive int array (must not be empty)
	 */
	public static int get(int[] array) {
		if (array.length == 0)
			throw new IllegalArgumentException("Cannot get random int from an empty array.");
		return array.length == 1 ? array[0] : array[nextInt(array.length)];
	}

	/**
	 * @see RandomGenerator#doubles()
	 */
	public static DoubleStream doubles() {
		return ThreadLocalRandom.current().doubles();
	}

	/**
	 * @see RandomGenerator#doubles(double, double)
	 */
	public static DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
		return ThreadLocalRandom.current().doubles(randomNumberOrigin, randomNumberBound);
	}

	/**
	 * @see RandomGenerator#ints()
	 */
	public static IntStream ints() {
		return ThreadLocalRandom.current().ints();
	}

	/**
	 * @see RandomGenerator#ints(int, int)
	 */
	public static IntStream ints(int randomNumberOrigin, int randomNumberBound) {
		return ThreadLocalRandom.current().ints(randomNumberOrigin, randomNumberBound);
	}

	/**
	 * @see RandomGenerator#longs()
	 */
	public static LongStream longs() {
		return ThreadLocalRandom.current().longs();
	}

	/**
	 * @see RandomGenerator#longs(long, long)
	 */
	public static LongStream longs(long randomNumberOrigin, long randomNumberBound) {
		return ThreadLocalRandom.current().longs(randomNumberOrigin, randomNumberBound);
	}

	/**
	 * @see RandomGenerator#nextBytes(byte[])
	 */
	public static void nextBytes(byte[] bytes) {
		ThreadLocalRandom.current().nextBytes(bytes);
	}

	/**
	 * @see RandomGenerator#nextInt()
	 */
	public static int nextInt() {
		return ThreadLocalRandom.current().nextInt();
	}

	/**
	 * @see RandomGenerator#nextInt(int)
	 */
	public static int nextInt(int bound) {
		return ThreadLocalRandom.current().nextInt(bound);
	}

	/**
	 * @see RandomGenerator#nextInt(int, int)
	 */
	public static int nextInt(int origin, int bound) {
		return ThreadLocalRandom.current().nextInt(origin, bound);
	}

	/**
	 * @see RandomGenerator#nextLong()
	 */
	public static long nextLong() {
		return ThreadLocalRandom.current().nextLong();
	}

	/**
	 * @see RandomGenerator#nextLong(long)
	 */
	public static long nextLong(long bound) {
		return ThreadLocalRandom.current().nextLong(bound);
	}

	/**
	 * @see RandomGenerator#nextLong(long, long)
	 */
	public static long nextLong(long origin, long bound) {
		return ThreadLocalRandom.current().nextLong(origin, bound);
	}

	/**
	 * @see RandomGenerator#nextFloat()
	 */
	public static float nextFloat() {
		return ThreadLocalRandom.current().nextFloat();
	}

	/**
	 * @see RandomGenerator#nextFloat(float)
	 */
	public static float nextFloat(float bound) {
		return ThreadLocalRandom.current().nextFloat(bound);
	}

	/**
	 * @see RandomGenerator#nextFloat(float, float)
	 */
	public static float nextFloat(float origin, float bound) {
		return ThreadLocalRandom.current().nextFloat(origin, bound);
	}

	/**
	 * @see RandomGenerator#nextDouble()
	 */
	public static double nextDouble() {
		return ThreadLocalRandom.current().nextDouble();
	}

	/**
	 * @see RandomGenerator#nextDouble(double)
	 */
	public static double nextDouble(double bound) {
		return ThreadLocalRandom.current().nextDouble(bound);
	}

	/**
	 * @see RandomGenerator#nextDouble(double, double)
	 */
	public static double nextDouble(double origin, double bound) {
		return ThreadLocalRandom.current().nextDouble(origin, bound);
	}

	/**
	 * @see RandomGenerator#nextBoolean()
	 */
	public static boolean nextBoolean() {
		return ThreadLocalRandom.current().nextBoolean();
	}
}
