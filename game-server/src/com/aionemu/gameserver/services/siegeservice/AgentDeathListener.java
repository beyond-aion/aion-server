package com.aionemu.gameserver.services.siegeservice;

import com.aionemu.gameserver.ai2.GeneralAIEvent;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventListener;
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
	public void onBeforeEvent(GeneralAIEvent event) {
		super.onBeforeEvent(event);
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
