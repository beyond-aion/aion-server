package com.aionemu.gameserver.model.team.group.callback;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.CallbackResult;
import com.aionemu.gameserver.model.team.group.PlayerGroup;

/**
 * @author ATracer
 */
@SuppressWarnings("rawtypes")
public abstract class PlayerGroupDisbandCallback implements Callback {

	@Override
	public CallbackResult beforeCall(Object obj, Object[] args) {
		onBeforeGroupDisband((PlayerGroup) args[0]);
		return CallbackResult.newContinue();
	}

	@Override
	public CallbackResult afterCall(Object obj, Object[] args, Object methodResult) {
		onAfterGroupDisband((PlayerGroup) args[0]);
		return CallbackResult.newContinue();
	}

	@Override
	public Class<? extends Callback> getBaseClass() {
		return PlayerGroupDisbandCallback.class;
	}

	public abstract void onBeforeGroupDisband(PlayerGroup group);

	public abstract void onAfterGroupDisband(PlayerGroup group);
}
