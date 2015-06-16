package com.aionemu.gameserver.ai2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author ATracer
 */
public class AI2Logger {

	private static final Logger log = LoggerFactory.getLogger(AI2Logger.class);

	public static final void info(AbstractAI ai, String message) {
		if (ai.isLogging()) {
			log.info("[AI2] " + ai.getOwner().getObjectId() + " - " + message);
		}
	}

	public static final void info(AI2 ai, String message) {
		info((AbstractAI) ai, message);
	}

	/**
	 * @param owner
	 * @param message
	 */
	public static void moveinfo(Creature owner, String message) {
		if (AIConfig.MOVE_DEBUG && owner.getAi2().isLogging()) {
			log.info("[AI2] " + owner.getObjectId() + " - " + message);
		}
	}
}
