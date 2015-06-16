package com.aionemu.gameserver.ai2;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public abstract class AI2Request {

    public abstract void acceptRequest(Creature requester, Player responder, int requestId);
	public void denyRequest(Creature requester, Player responder) {
	};
}
