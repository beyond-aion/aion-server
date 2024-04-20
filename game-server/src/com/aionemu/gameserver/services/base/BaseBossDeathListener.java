package com.aionemu.gameserver.services.base;

import com.aionemu.gameserver.ai.GeneralAIEvent;
import com.aionemu.gameserver.ai.eventcallback.OnDieEventListener;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.services.BaseService;

/**
 * @author Source, Rolandas
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

		Race winnerRace = Race.NPC;
		if (winner instanceof Creature) {
			Creature kill = (Creature) winner;
			if (kill.getRace().isAsmoOrEly())
				winnerRace = kill.getRace();
		} else if (winner instanceof TemporaryPlayerTeam) {
			winnerRace = ((TemporaryPlayerTeam<?>) winner).getRace();
		}

		if (winnerRace == base.getRace())
			throw new BaseException("Base boss got killed by its own race! Boss killer: " + winner + ", Base ID: " + base.getId());
		BaseService.getInstance().capture(base.getId(), winnerRace);
	}

}
