package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;

/**
 * @author ATracer
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BleedEffect")
public class BleedEffect extends AbstractOverTimeEffect {
	
	
	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, StatEnum.BLEED_RESISTANCE, null);
	}

	@Override
	public void startEffect(final Effect effect) {
		int valueWithDelta = value + delta * effect.getSkillLevel();
		int critAddDmg = this.critAddDmg2 + this.critAddDmg1 * effect.getSkillLevel();
		int finalDamage = AttackUtil.calculateMagicalOverTimeSkillResult(effect, valueWithDelta, element, this.position, false, this.critProbMod2, critAddDmg);
		effect.setReserveds(new EffectReserved(position, finalDamage, "HP", true, false), true);
		super.startEffect(effect, AbnormalState.BLEED);
	}
	
	@Override
	public void endEffect(Effect effect) {
		super.endEffect(effect, AbnormalState.BLEED);
	}

	@Override
	public void onPeriodicAction(Effect effect) {
		Creature effected = effect.getEffected();
		Creature effector = effect.getEffector();
		effected.getController().onAttack(effector, effect.getSkillId(), TYPE.DAMAGE, effect.getReserveds(position).getValue(), false, LOG.BLEED);
		effected.getObserveController().notifyDotAttackedObservers(effector, effect);
	}
}
