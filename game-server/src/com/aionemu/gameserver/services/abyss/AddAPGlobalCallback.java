package com.aionemu.gameserver.services.abyss;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.CallbackResult;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;

/**
 * @author Rolandas
 */
@SuppressWarnings("rawtypes")
public abstract class AddAPGlobalCallback implements Callback {

	@Override
	public CallbackResult beforeCall(Object obj, Object[] args) {
		return CallbackResult.newContinue();
	}

	@Override
	public CallbackResult afterCall(Object obj, Object[] args, Object methodResult) {
		Player player = (Player) args[0];
		VisibleObject creature = (VisibleObject) args[1];
		int abyssPoints = (Integer) args[2];

		// Only Players or SiegeNpc(from SiegeModType.SIEGE or .ASSAULT) can add points
		if (creature instanceof Player) {
			onAbyssPointsAdded(player, abyssPoints);
		}
		else if (creature instanceof SiegeNpc) {
			if (!((SiegeNpc) creature).getSpawn().isPeace())
				onAbyssPointsAdded(player, abyssPoints);
		}

		return CallbackResult.newContinue();
	}

	@Override
	public Class<? extends Callback> getBaseClass() {
		return AddAPGlobalCallback.class;
	}

	public abstract void onAbyssPointsAdded(Player player, int abyssPoints);
}
