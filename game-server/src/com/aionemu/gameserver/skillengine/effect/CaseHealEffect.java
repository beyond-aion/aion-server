package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.container.StatEnum;
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
	protected int getCurrentStatValue(Effect effect) {
		if (type == HealType.HP)
			return effect.getEffected().getLifeStats().getCurrentHp();
		else if (type == HealType.MP)
			return effect.getEffected().getLifeStats().getCurrentMp();

		return 0;
	}

	@Override
	protected int getMaxStatValue(Effect effect) {
		if (type == HealType.HP)
			return effect.getEffected().getGameStats().getMaxHp().getCurrent();
		else if (type == HealType.MP)
			return effect.getEffected().getGameStats().getMaxMp().getCurrent();

		return 0;
	}

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void endEffect(Effect effect) {
		ActionObserver observer = effect.getActionObserver(position);
		if (observer != null)
			effect.getEffected().getObserveController().removeObserver(observer);
	}

	@Override
	public void startEffect(final Effect effect) {
		ActionObserver observer = new ActionObserver(ObserverType.ATTACKED) {

			@Override
			public void attacked(Creature creature, int skillId) {
				calculateHeal(effect);
			}
		};
		effect.getEffected().getObserveController().addObserver(observer);
		effect.setActionObserver(observer, position);
		calculateHeal(effect);
	}

	private void calculateHeal(final Effect effect) {
		final int valueWithDelta = value + delta * effect.getSkillLevel();
		final int currentValue = getCurrentStatValue(effect);
		final int maxValue = getMaxStatValue(effect);
		if (currentValue <= (maxValue * condValue / 100)) {
			int possibleHealValue = 0;
			if (percent)
				possibleHealValue = maxValue * valueWithDelta / 100;
			else
				possibleHealValue = valueWithDelta;

			int finalHeal = effect.getEffected().getGameStats().getStat(StatEnum.HEAL_SKILL_BOOST, possibleHealValue).getCurrent();
			finalHeal = effect.getEffected().getGameStats().getStat(StatEnum.HEAL_SKILL_DEBOOST, finalHeal).getCurrent();
			finalHeal = maxValue - currentValue < finalHeal ? (maxValue - currentValue) : finalHeal;

			if (type == HealType.HP && effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.DISEASE))
				finalHeal = 0;

			// apply heal
			if (type == HealType.HP)
				effect.getEffected().getLifeStats().increaseHp(TYPE.HP, finalHeal, effect.getSkillId(), LOG.CASEHEAL);
			else if (type == HealType.MP)
				effect.getEffected().getLifeStats().increaseMp(TYPE.MP, finalHeal, effect.getSkillId(), LOG.CASEHEAL);// TODO check
			effect.endEffect();
		}
	}
}
