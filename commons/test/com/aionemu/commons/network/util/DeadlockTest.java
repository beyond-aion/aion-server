package com.aionemu.commons.network.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This test is for checking print of deadlock report. Should not be executed during unit test phase
 * 
 * @author ATracer, Neon
 */
public class DeadlockTest {

	private static final Object lock1 = new Object();
	private static final Object lock2 = new Object();

	public static void main(String... args) {
		DeadLockDetector dd = new DeadLockDetector(2, DeadLockDetector.NOTHING);
		dd.start();
		createDeadlock();
	}

	/**
	 * This complex logic is just to generate a longer stacktrace
	 */
	private static void createDeadlock() {
		List<String> coll = new ArrayList<>(Arrays.asList("1"));
		synchronized (lock1) {
			coll.stream().mapToInt(Integer::valueOf).forEach(intValue -> {

					new Thread(new Runnable() {

						@Override
						public void run() {
							System.out.println("Locking lock 2 from thread 2");
							synchronized (lock2) {
								System.out.println("Deadlocking");
								synchronized (lock1) {
									System.out.println("This will not be printed");
								}
							}
						}
					}).start();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized (lock2) {
						System.out.println("This will not be printed");
					}
			});
		}
	}
}
