package com.aionemu.gameserver.model;

/**
 * @author ATracer
 */
public interface GameEngine {

	/**
	 * Load resources for engine
	 */
	void load();

	/**
	 * Cleanup resources for engine
	 */
	void shutdown();
}
