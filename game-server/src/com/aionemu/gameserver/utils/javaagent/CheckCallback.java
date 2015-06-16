package com.aionemu.gameserver.utils.javaagent;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.CallbackResult;

/**
 * @author Rolandas
 */
@SuppressWarnings("rawtypes")
public class CheckCallback implements Callback {

	@Override
	public CallbackResult<Boolean> beforeCall(Object obj, Object[] args) {
		return CallbackResult.newFullBlocker(true);
	}

	@Override
	public CallbackResult<Boolean> afterCall(Object obj, Object[] args, Object methodResult) {
		return CallbackResult.newContinue();
	}

	@Override
	public Class<? extends Callback> getBaseClass() {
		return CheckCallback.class;
	}
}
