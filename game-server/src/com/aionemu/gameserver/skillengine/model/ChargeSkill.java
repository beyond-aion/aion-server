package com.aionemu.gameserver.skillengine.model;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.skillengine.properties.Properties.CastState;

/**
 * @author Cheatkiller
 */
public class ChargeSkill extends Skill {

	public ChargeSkill(SkillTemplate skillTemplate, Player effector, int skillLevel, Creature firstTarget, ItemTemplate itemTemplate) {
		super(skillTemplate, effector, skillLevel, firstTarget, null);
	}

	@Override
	public void calculateAndSetCastDuration() {
	}

	@Override
	public boolean useSkill() {
		if (!canUseSkill(CastState.CAST_END)) {
			effector.getController().cancelCurrentSkill(null);
			return false;
		}
		effector.getObserveController().notifyBoostSkillCostObservers(this);
		effector.getObserveController().notifyStartSkillCastObservers(this);
		effector.setCasting(this);
		effector.getObserveController().attach(moveListener);
		endCast();
		return true;
	}

	@Override
	protected void endCast() {
		super.endCast();
		if (effector instanceof Player player) {
			float temporaryAdjustmentFactor = 0.8f; // TODO remove after fixing motion validation (see 1dabdd7)
			player.setNextSkillUse(System.currentTimeMillis() + (long) (getAnimationTime() * temporaryAdjustmentFactor));
		}
	}
}
