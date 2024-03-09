package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HealType;

/**
 * @author kecimis
 */
public class CaseHealEffect extends AbstractHealEffect {

	@XmlAttribute(name = "cond_value")
	protected int condValue;
	@XmlAttribute
	protected HealType type;

	@Override
	public int getCurrentStatValue(Effect effect) {
		return switch (type) {
			case HP -> effect.getEffected().getLifeStats().getCurrentHp();
			case MP -> effect.getEffected().getLifeStats().getCurrentMp();
			default -> 0;
		};
	}

	@Override
	public int getMaxStatValue(Effect effect) {
		return switch (type) {
			case HP -> effect.getEffected().getGameStats().getMaxHp().getCurrent();
			case MP -> effect.getEffected().getGameStats().getMaxMp().getCurrent();
			default -> 0;
		};
	}

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(final Effect effect) {
		if (tryHeal(effect))
			return;
		effect.addObserver(effect.getEffected(), new ActionObserver(ObserverType.HP_CHANGED) {

			@Override
			public void hpChanged(int value) {
				tryHeal(effect);
			}

		});
	}

	private boolean tryHeal(final Effect effect) {
		final int currentValue = getCurrentStatValue(effect);
		final int maxCurValue = getMaxStatValue(effect);
		// only heal if the current value is at or below the given percentage
		if (currentValue <= (maxCurValue * condValue / 100f)) {
			if (type == HealType.HP)
				effect.getEffected().getLifeStats().increaseHp(TYPE.HP, calculateHealValue(effect, type), effect, LOG.CASEHEAL);
			else if (type == HealType.MP)
				effect.getEffected().getLifeStats().increaseMp(TYPE.MP, calculateHealValue(effect, type), effect.getSkillId(), LOG.CASEHEAL);
			effect.endEffect();
			return true;
		}
		return false;
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
