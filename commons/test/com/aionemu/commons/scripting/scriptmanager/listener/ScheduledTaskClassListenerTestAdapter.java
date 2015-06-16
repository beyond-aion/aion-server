package com.aionemu.commons.scripting.scriptmanager.listener;

import com.aionemu.commons.scripting.classlistener.ScheduledTaskClassListener;
import com.aionemu.commons.services.CronService;

public class ScheduledTaskClassListenerTestAdapter extends ScheduledTaskClassListener {

	private final CronService cronService;

	public ScheduledTaskClassListenerTestAdapter(CronService cronService) {
		this.cronService = cronService;
	}

	@Override
	protected CronService getCronService() {
		return cronService;
	}
}
