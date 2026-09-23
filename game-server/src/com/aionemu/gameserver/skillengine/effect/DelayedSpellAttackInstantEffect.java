package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackUtil;
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

		AttackUtil.calculateSkillResult(effect, valueWithDelta, this, true); // ignores shields on retail
		final int finalPosition = this.position;
		ThreadPoolManager.getInstance().schedule(() ->  {
				effect.getEffected().getController().onAttack(effect, TYPE.DELAYDAMAGE, effect.getReserveds(finalPosition).getValue(), true,
					LOG.DELAYEDSPELLATKINSTANT, hopType);
				effect.getEffector().getObserveController().notifyAttackObservers(effect.getEffected(), effect.getSkillId());
		}, getRemainingDelay(effect, delay));
	}

	/**
	 * @return The part of {@code delay}, counted from the end of the cast, which has not yet passed when the effects of the skill are applied.
	 */
	static int getRemainingDelay(Effect effect, int delay) {
		return effect.getSkill() == null ? delay : Math.max(0, delay - effect.getSkill().getLandingDelay());
	}

	public int getDelay() {
		return delay;
	}

	@Override
	public void calculateDamage(Effect effect) {
	}
}
