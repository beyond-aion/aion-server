package com.aionemu.gameserver.model.team2.group.callback;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.CallbackResult;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
@SuppressWarnings("rawtypes")
public abstract class PlayerGroupCreateCallback implements Callback {

	@Override
	public CallbackResult beforeCall(Object obj, Object[] args) {
		onBeforeGroupCreate((Player) args[0]);
		return CallbackResult.newContinue();
	}

	@Override
	public CallbackResult afterCall(Object obj, Object[] args, Object methodResult) {
		onAfterGroupCreate((Player) args[0]);
		return CallbackResult.newContinue();
	}

	@Override
	public Class<? extends Callback> getBaseClass() {
		return PlayerGroupCreateCallback.class;
	}

	public abstract void onBeforeGroupCreate(Player player);

	public abstract void onAfterGroupCreate(Player player);
}
