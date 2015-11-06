package com.aionemu.gameserver.services.base;

import com.aionemu.gameserver.ai2.GeneralAIEvent;
import com.aionemu.gameserver.ai2.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;
import com.aionemu.gameserver.services.BaseService;

/**
 * @author Source
 * @modified Rolandas
 */
public class BaseBossDeathListener extends OnDieEventListener {

	private final Base<?> base;

	public BaseBossDeathListener(Base<?> base) {
		this.base = base;
	}

	@Override
	public void onBeforeEvent(GeneralAIEvent event) {
		super.onBeforeEvent(event);
		if (!event.isHandled())
			return;

		AionObject winner = event.getSource().getOwner().getAggroList().getMostDamage();

		if (winner instanceof Creature) {
			Creature kill = (Creature) winner;
			if (kill.getRace().isPlayerRace())
				base.setLocRace(kill.getRace());
		}
		else if (winner instanceof TemporaryPlayerTeam) {
			TemporaryPlayerTeam<?> team = (TemporaryPlayerTeam<?>) winner;
			if (team.getRace().isPlayerRace())
				base.setLocRace(team.getRace());
		}
		else
			base.setLocRace(Race.NPC);

		BaseService.getInstance().capture(base.getId());
	}

	@Override
	public void onAfterEvent(GeneralAIEvent event) {
	}

}

