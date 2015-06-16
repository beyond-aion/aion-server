package com.aionemu.gameserver.world.knownlist;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public class PlayerAwareKnownList extends KnownList {

	public PlayerAwareKnownList(VisibleObject owner) {
		super(owner);
	}

	@Override
	protected final boolean isAwareOf(VisibleObject newObject) {
		return newObject instanceof Player;
	}

}
