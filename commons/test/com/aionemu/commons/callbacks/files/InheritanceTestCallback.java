package com.aionemu.commons.callbacks.files;

import com.aionemu.commons.callbacks.Callback;
import com.aionemu.commons.callbacks.CallbackResult;

@SuppressWarnings("rawtypes")
public class InheritanceTestCallback implements Callback<InheritanceTestSuperclass> {

	private final String res;

	public InheritanceTestCallback(String res) {
		this.res = res;
	}

	@Override
	public CallbackResult beforeCall(InheritanceTestSuperclass obj, Object[] args) {
		return CallbackResult.newContinue();
	}

	@Override
	public CallbackResult afterCall(InheritanceTestSuperclass obj, Object[] args, Object methodResult) {
		return CallbackResult.newFullBlocker(res);
	}

	@Override
	public Class<? extends Callback> getBaseClass() {
		return InheritanceTestCallback.class;
	}
}
