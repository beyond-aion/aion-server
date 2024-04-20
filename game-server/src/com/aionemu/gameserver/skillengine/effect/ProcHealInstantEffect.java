package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HealType;

/**
 * @author ATracer, Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcHealInstantEffect")
public class ProcHealInstantEffect extends AbstractHealEffect {

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, HealType.HP);
	}

	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect, HealType.HP);
	}

	@Override
	public int getCurrentStatValue(Effect effect) {
		return effect.getEffected().getLifeStats().getCurrentHp();
	}

	@Override
	public int getMaxStatValue(Effect effect) {
		return effect.getEffected().getGameStats().getMaxHp().getCurrent();
	}

	@Override
	public boolean allowHpHealBoost(Effect effect) {
		return false;
	}

	@Override
	public boolean allowHpHealSkillDeboost(Effect effect) {
		return false;
	}
}
