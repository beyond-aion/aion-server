package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.NpcAI;

/**
 * @author ATracer
 */
public class MoveEventHandler {

	/**
	 * @param npcAI
	 */
	public static final void onMoveValidate(NpcAI npcAI) {
		if (npcAI.getOwner().canPerformMove()) {
			npcAI.getOwner().getController().onMove();
			TargetEventHandler.onTargetTooFar(npcAI);
		}
	}

	/**
	 * @param npcAI
	 */
	public static final void onMoveArrived(NpcAI npcAI) {
		npcAI.getOwner().getController().onMove();
		TargetEventHandler.onTargetReached(npcAI);
	}

}
