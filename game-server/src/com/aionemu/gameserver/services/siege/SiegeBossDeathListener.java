package com.aionemu.gameserver.services.siege;

import com.aionemu.gameserver.ai.GeneralAIEvent;
import com.aionemu.gameserver.ai.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.services.SiegeService;

public class SiegeBossDeathListener extends OnDieEventListener {

	private final Siege<?> siege;

	public SiegeBossDeathListener(Siege<?> siege) {
		this.siege = siege;
	}

	@Override
	public void onAfterEvent(GeneralAIEvent event) {
		if (event.isHandled()) {
			siege.setBossKilled(true);
			SiegeService.getInstance().stopSiege(siege.getSiegeLocationId());
		}
	}

}
