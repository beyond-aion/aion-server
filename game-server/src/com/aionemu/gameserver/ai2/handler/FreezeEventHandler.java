package com.aionemu.gameserver.ai2.handler;

import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.ai2.AbstractAI;

/**
 * @author Rolandas
 * @modified Neon
 */
public class FreezeEventHandler {

	public static void onUnfreeze(AbstractAI ai) {
		if (ai.isInSubState(AISubState.FREEZE)) {
			ai.setSubStateIfNot(AISubState.NONE);
			ai.think();
		}
	}

	public static void onFreeze(AbstractAI ai) {
		ai.setSubStateIfNot(AISubState.FREEZE);
		ai.think();
	}
}
