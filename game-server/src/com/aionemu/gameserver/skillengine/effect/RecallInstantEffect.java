package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.RecallService;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Bio, Sippolo, SVDNESS
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecallInstantEffect")
public class RecallInstantEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		if (effect.getEffector() instanceof Player caster && effect.getEffected() instanceof Player effected)
			RecallService.getInstance().requestSummon(caster, effected, effect.getSkillId());
	}

	@Override
	public void calculate(Effect effect) {
		Creature effector = effect.getEffector();
		if (RecallService.canBeSummoned(effector, effect.getEffected())) {
			effect.getSkill().setTargetPosition(effector.getX(), effector.getY(), effector.getZ(), effector.getHeading());
			effect.addSuccessEffect(this);
		}
	}
}
