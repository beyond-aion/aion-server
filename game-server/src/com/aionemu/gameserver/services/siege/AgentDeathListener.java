package com.aionemu.gameserver.services.siege;

import com.aionemu.gameserver.ai.GeneralAIEvent;
import com.aionemu.gameserver.ai.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.services.SiegeService;

/**
 * @author Estrayl
 *
 */
public class AgentDeathListener extends OnDieEventListener {
	
	private AgentSiege siege;
	private SiegeRace race;
	
	public AgentDeathListener(AgentSiege siege) {
		this.siege = siege;
	}

	@Override
	public void onAfterEvent(GeneralAIEvent event) {
		if (event.isHandled()) {
			siege.setWinnerRace(race);
			SiegeService.getInstance().stopSiege(siege.getSiegeLocationId());
		}
	}

	public void setRace(SiegeRace race) {
		this.race = race;
	}
}
