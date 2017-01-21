package com.aionemu.gameserver.model.team.alliance.callback;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.CallbackResult;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;

/**
 * @author ATracer
 */
@SuppressWarnings("rawtypes")
public abstract class PlayerAllianceDisbandCallback implements Callback {

	@Override
	public CallbackResult beforeCall(Object obj, Object[] args) {
		onBeforeAllianceDisband((PlayerAlliance) args[0], (boolean) args[1]);
		return CallbackResult.newContinue();
	}

	@Override
	public CallbackResult afterCall(Object obj, Object[] args, Object methodResult) {
		onAfterAllianceDisband((PlayerAlliance) args[0], (boolean) args[1]);
		return CallbackResult.newContinue();
	}

	@Override
	public Class<? extends Callback> getBaseClass() {
		return PlayerAllianceDisbandCallback.class;
	}

	public abstract void onBeforeAllianceDisband(PlayerAlliance alliance, boolean onBeforeOnly);

	public abstract void onAfterAllianceDisband(PlayerAlliance alliance, boolean onBeforeOnly);

}
