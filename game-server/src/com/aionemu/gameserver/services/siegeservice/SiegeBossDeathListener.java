package com.aionemu.gameserver.services.siegeservice;

import com.aionemu.gameserver.ai2.GeneralAIEvent;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.services.SiegeService;

public class SiegeBossDeathListener extends OnDieEventListener {

	private final Siege<?> siege;

	public SiegeBossDeathListener(Siege<?> siege) {
		this.siege = siege;
	}

	@Override
	public void onBeforeEvent(GeneralAIEvent event) {
		super.onBeforeEvent(event);
	}

	@Override
	public void onAfterEvent(GeneralAIEvent event) {
		if (event.isHandled()) {
			siege.setBossKilled(true);
			SiegeService.getInstance().stopSiege(siege.getSiegeLocationId());
		}
	}

}
