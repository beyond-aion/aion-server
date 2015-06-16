package com.aionemu.gameserver.model;

import java.util.concurrent.CountDownLatch;

/**
 * @author ATracer
 */
public interface GameEngine {

	/**
	 * Load resources for engine
	 */
	void load(CountDownLatch progressLatch);

	/**
	 * Cleanup resources for engine
	 */
	void shutdown();
}
