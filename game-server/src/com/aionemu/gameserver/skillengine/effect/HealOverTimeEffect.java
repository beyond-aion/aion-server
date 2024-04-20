package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;
import com.aionemu.gameserver.skillengine.model.EffectReserved.ResourceType;
import com.aionemu.gameserver.skillengine.model.HealType;

/**
 * @author ATracer, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HealOverTimeEffect")
public abstract class HealOverTimeEffect extends AbstractOverTimeEffect implements HealEffectTemplate {

	@Override
	public void calculate(Effect effect) {
		if (!super.calculate(effect, null, null))
			return;

		effect.addSuccessEffect(this);
	}

	public void startEffect(Effect effect, HealType healType) {
		effect.setReserveds(new EffectReserved(position, calculateHealValue(effect, healType), ResourceType.of(healType), false, false), true);
		super.startEffect(effect, null);
	}

	public void onPeriodicAction(Effect effect, HealType healType) {
		Creature effected = effect.getEffected();

		int currentValue = getCurrentStatValue(effect);
		int maxCurValue = getMaxStatValue(effect);
		int possibleHealValue = effect.getReserveds(position).getValue();

		if (healType == HealType.HP && effect.getItemTemplate() == null)
			possibleHealValue = effected.getGameStats().getStat(StatEnum.HEAL_SKILL_DEBOOST, possibleHealValue).getCurrent();

		int healValue = maxCurValue - currentValue < possibleHealValue ? (maxCurValue - currentValue) : possibleHealValue;

		if (healValue <= 0)
			return;

		switch (healType) {
			case HP -> effected.getLifeStats().increaseHp(TYPE.HP, healValue, effect, LOG.HEAL);
			case MP -> effected.getLifeStats().increaseMp(TYPE.MP, healValue, effect.getSkillId(), LOG.MPHEAL);
			case FP -> ((Player) effected).getLifeStats().increaseFp(TYPE.FP, healValue, effect.getSkillId(), LOG.FPHEAL);
			case DP -> ((Player) effected).getCommonData().addDp(healValue);
		}

	}

	@Override
	public boolean isPercent() {
		return percent;
	}

	@Override
	public boolean allowHpHealBoost(Effect effect) {
		return !percent && effect.getItemTemplate() == null;
	}

	@Override
	public boolean allowHpHealSkillDeboost(Effect effect) {
		return false; // calculated in onPeriodicAction instead
	}

	@Override
	public int calculateBaseHealValue(Effect effect) {
		return calculateBaseValue(effect);
	}
}
