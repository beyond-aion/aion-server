package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.services.summons.TrapService;

public class TrapController extends NpcController {

	@Override
	public void onDie(Creature lastAttacker, boolean sendDiePacket) {
		TrapService.unregisterTrap(getOwner().getObjectId());
		super.onDie(lastAttacker, sendDiePacket);
	}

	@Override
	public void onDelete() {
		TrapService.unregisterTrap(getOwner().getObjectId());
		super.onDelete();
	}

}
