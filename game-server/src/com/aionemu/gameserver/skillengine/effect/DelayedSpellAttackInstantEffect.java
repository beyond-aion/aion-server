package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DelayedSpellAttackInstantEffect")
public class DelayedSpellAttackInstantEffect extends DamageEffect {

	@XmlAttribute
	protected int delay;

	@Override
	public void applyEffect(Effect effect) {
		int valueWithDelta = calculateBaseValue(effect);
		if (element != SkillElement.NONE)
			valueWithDelta *= effect.getEffector().getGameStats().getKnowledge().getCurrent() / 100;

		AttackUtil.calculateSkillResult(effect, valueWithDelta, this, true); // ignores shields on retail
		final int finalPosition = this.position;
		ThreadPoolManager.getInstance().schedule(() ->  {
				effect.getEffected().getController().onAttack(effect, TYPE.DELAYDAMAGE, effect.getReserveds(finalPosition).getValue(), true,
					LOG.DELAYEDSPELLATKINSTANT, hopType);
				effect.getEffector().getObserveController().notifyAttackObservers(effect.getEffected(), effect.getSkillId());
		}, delay);
	}

	@Override
	public void calculateDamage(Effect effect) {
	}
}
