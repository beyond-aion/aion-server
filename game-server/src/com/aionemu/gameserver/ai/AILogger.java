package com.aionemu.gameserver.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.AIConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author ATracer
 */
public class AILogger {

	private static final Logger log = LoggerFactory.getLogger(AILogger.class);

	public static final void info(AbstractAI<? extends Creature> ai, String message) {
		if (ai.isLogging()) {
			log.info("[AI] " + ai.getOwner().getObjectId() + " - " + message);
		}
	}

	/**
	 * @param owner
	 * @param message
	 */
	public static void moveinfo(Creature owner, String message) {
		if (AIConfig.MOVE_DEBUG && owner.getAi().isLogging()) {
			log.info("[AI] " + owner.getObjectId() + " - " + message);
		}
	}
}
