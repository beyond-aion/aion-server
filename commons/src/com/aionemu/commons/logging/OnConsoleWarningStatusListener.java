package com.aionemu.commons.logging;

import ch.qos.logback.core.status.OnErrorConsoleStatusListener;
import ch.qos.logback.core.status.Status;

/**
 * Suppresses all informational messages
 */
public class OnConsoleWarningStatusListener extends OnErrorConsoleStatusListener {

	public OnConsoleWarningStatusListener() {
		setRetrospective(0);
	}

	@Override
	public void addStatusEvent(Status status) {
		if (status.getLevel() >= Status.WARN) {
			super.addStatusEvent(status);
		}
	}
}
