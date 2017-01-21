package com.aionemu.gameserver.ai;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public abstract class AIRequest {

	public abstract void acceptRequest(Creature requester, Player responder, int requestId);

	public void denyRequest(Creature requester, Player responder) {
	};
}
