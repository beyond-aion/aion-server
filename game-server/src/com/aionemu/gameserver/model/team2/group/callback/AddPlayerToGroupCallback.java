package com.aionemu.gameserver.model.team2.group.callback;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.CallbackResult;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;

/**
 * @author ATracer
 */
@SuppressWarnings("rawtypes")
public abstract class AddPlayerToGroupCallback implements Callback {

	@Override
	public CallbackResult beforeCall(Object obj, Object[] args) {
		onBeforePlayerAddToGroup((PlayerGroup) args[0], (Player) args[1]);
		return CallbackResult.newContinue();
	}

	@Override
	public CallbackResult afterCall(Object obj, Object[] args, Object methodResult) {
		onAfterPlayerAddToGroup((PlayerGroup) args[0], (Player) args[1]);
		return CallbackResult.newContinue();
	}

	@Override
	public Class<? extends Callback> getBaseClass() {
		return AddPlayerToGroupCallback.class;
	}

	public abstract void onBeforePlayerAddToGroup(PlayerGroup group, Player player);
	
	public abstract void onAfterPlayerAddToGroup(PlayerGroup group, Player player);

}
