package com.aionemu.gameserver.services.monsterraid;

import com.aionemu.gameserver.ai2.GeneralAIEvent;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.services.MonsterRaidService;


/**
 * @author Whoop
 *
 */
public class MonsterRaidBossDeathListener extends OnDieEventListener {
	
	private final MonsterRaid monsterRaid;

	public MonsterRaidBossDeathListener(MonsterRaid monsterRaid) {
		this.monsterRaid = monsterRaid;
	}

	@Override
	public void onBeforeEvent(GeneralAIEvent event) {
		super.onBeforeEvent(event);
	}

	@Override
	public void onAfterEvent(GeneralAIEvent event) {
		if (event.isHandled()) {
			monsterRaid.setBossKilled(true);
			MonsterRaidService.getInstance().stopRaid(monsterRaid.getMonsterRaidLocationId());
		}
	}
}