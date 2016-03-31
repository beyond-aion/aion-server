package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author Yeats 04.03.2016.
 */
public class DieObserver extends ActionObserver {

	private Skill skill;

	public DieObserver(Skill skill) {
		super(ObserverType.DEATH);
		this.skill = skill;
	}

	@Override
	public void died(Creature creature) {
		if (skill != null) {
			skill.getEffector().getController().cancelCurrentSkill(null, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_LOST());
		}
	}
}
