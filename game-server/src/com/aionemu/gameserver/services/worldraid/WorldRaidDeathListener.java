package com.aionemu.gameserver.services.worldraid;

import com.aionemu.gameserver.ai.GeneralAIEvent;
import com.aionemu.gameserver.ai.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.services.WorldRaidService;

/**
 * @author Whoop
 */
public class WorldRaidDeathListener extends OnDieEventListener {

	private final WorldRaid worldRaid;

	public WorldRaidDeathListener(WorldRaid worldRaid) {
		this.worldRaid = worldRaid;
	}

	@Override
	public void onAfterEvent(GeneralAIEvent event) {
		if (event.isHandled()) {
			worldRaid.setBossKilled(true);
			WorldRaidService.getInstance().stopRaid(worldRaid.getLocationId());
		}
	}
}
