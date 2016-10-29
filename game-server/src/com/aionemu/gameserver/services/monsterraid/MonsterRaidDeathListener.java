package com.aionemu.gameserver.services.monsterraid;

import com.aionemu.gameserver.ai2.GeneralAIEvent;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.services.MonsterRaidService;

/**
 * @author Whoop
 */
public class MonsterRaidDeathListener extends OnDieEventListener {

	private final MonsterRaid monsterRaid;

	public MonsterRaidDeathListener(MonsterRaid monsterRaid) {
		this.monsterRaid = monsterRaid;
	}

	@Override
	public void onAfterEvent(GeneralAIEvent event) {
		if (event.isHandled()) {
			monsterRaid.setBossKilled(true);
			MonsterRaidService.getInstance().stopRaid(monsterRaid.getLocationId());
		}
	}
}
