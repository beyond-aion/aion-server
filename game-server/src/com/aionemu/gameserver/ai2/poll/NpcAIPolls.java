package com.aionemu.gameserver.ai2.poll;

import com.aionemu.gameserver.ai2.NpcAI2;

/**
 * @author ATracer
 */
public class NpcAIPolls {

	/**
	 * @param npcAI
	 */
	public static AIAnswer shouldDecay(NpcAI2 npcAI) {
		return AIAnswers.POSITIVE;
	}

	/**
	 * @param npcAI
	 */
	public static AIAnswer shouldRespawn(NpcAI2 npcAI) {
		return AIAnswers.POSITIVE;
	}

}
