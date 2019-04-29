package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.change.Func;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DamageEffect")
public abstract class DamageEffect extends EffectTemplate {

	@XmlAttribute
	protected Func mode = Func.ADD;
	@XmlAttribute
	protected boolean shared;

	@Override
	public void applyEffect(Effect effect) {
		effect.getEffected().getController().onAttack(effect, TYPE.REGULAR, effect.getReserveds(this.position).getValue(), true, LOG.REGULAR);
		effect.getEffector().getObserveController().notifyAttackObservers(effect.getEffected());
	}

	@Override
	public void calculateDamage(Effect effect) {
		int valueWithDelta = calculateBaseValue(effect);

		switch (element) {
			case NONE:
				valueWithDelta *= effect.getEffector().getGameStats().getPower().getCurrent() * 0.01f;
				break;
			default:
				valueWithDelta *= effect.getEffector().getGameStats().getKnowledge().getCurrent() * 0.01f;
				break;
		}

		AttackUtil.calculateSkillResult(effect, valueWithDelta, this, false);
	}

	public Func getMode() {
		return mode;
	}

	/**
	 * @return the shared
	 */
	public boolean isShared() {
		return shared;
	}
}
