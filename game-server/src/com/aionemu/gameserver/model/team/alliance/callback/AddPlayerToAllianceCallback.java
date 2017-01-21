package com.aionemu.gameserver.model.team.alliance.callback;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.CallbackResult;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;

/**
 * @author ATracer
 */
@SuppressWarnings("rawtypes")
public abstract class AddPlayerToAllianceCallback implements Callback {

	@Override
	public CallbackResult beforeCall(Object obj, Object[] args) {
		onBeforePlayerAddToAlliance((PlayerAlliance) args[0], (Player) args[1]);
		return CallbackResult.newContinue();
	}

	@Override
	public CallbackResult afterCall(Object obj, Object[] args, Object methodResult) {
		onAfterPlayerAddToAlliance((PlayerAlliance) args[0], (Player) args[1]);
		return CallbackResult.newContinue();
	}

	@Override
	public Class<? extends Callback> getBaseClass() {
		return AddPlayerToAllianceCallback.class;
	}

	public abstract void onBeforePlayerAddToAlliance(PlayerAlliance alliance, Player player);

	public abstract void onAfterPlayerAddToAlliance(PlayerAlliance alliance, Player player);

}
