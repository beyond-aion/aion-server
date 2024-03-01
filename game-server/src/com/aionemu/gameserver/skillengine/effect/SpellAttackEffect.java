package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;
import com.aionemu.gameserver.skillengine.model.EffectReserved.ResourceType;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpellAttackEffect")
public class SpellAttackEffect extends AbstractOverTimeEffect {

	@Override
	public void startEffect(Effect effect) {
		int valueWithDelta = calculateBaseValue(effect);
		int critAddDmg = critAddDmg2 + critAddDmg1 * effect.getSkillLevel();
		int finalDamage = AttackUtil.calculateMagicalOverTimeSkillResult(effect, valueWithDelta, element, position, useMagicBoost(effect), critProbMod2,
			critAddDmg);
		effect.setReserveds(new EffectReserved(position, finalDamage, ResourceType.HP, true, false), true);
		super.startEffect(effect);
	}

	@Override
	public void onPeriodicAction(Effect effect) {
		Creature effected = effect.getEffected();
		effected.getController().onAttack(effect, TYPE.DAMAGE, effect.getReserveds(position).getValue(), false, LOG.SPELLATK, hopType);
		effected.getObserveController().notifyDotAttackedObservers(effect.getEffector(), effect);
	}

	/**
	 * Retail server templates got values like:
	 * <apply_heal_boost_bonus>0</apply_heal_boost_bonus>
	 * <apply_magical_skill_boost_bonus>0</apply_magical_skill_boost_bonus>
	 * <apply_magical_critical>0</apply_magical_critical>
	 * If we succeeded in parsing the data, these exclusions can be removed.
	 */
	private boolean useMagicBoost(Effect effect) {
		return effect.getSkillId() != 21110; // Shugo Venom
	}
}
