package com.aionemu.gameserver.skillengine.effect;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatRateFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatSetFunction;
import com.aionemu.gameserver.model.stats.container.CreatureGameStats;
import com.aionemu.gameserver.skillengine.change.Change;
import com.aionemu.gameserver.skillengine.condition.Conditions;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BufEffect")
public abstract class BufEffect extends EffectTemplate {

	@XmlAttribute
	protected boolean maxstat;

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	/**
	 * Will be called from effect controller when effect starts
	 */
	@Override
	public void startEffect(Effect effect) {
		Creature effected = effect.getEffected();
		CreatureGameStats<? extends Creature> cgs = effected.getGameStats();

		List<IStatFunction> modifiers = getModifiers(effect);

		if (modifiers.size() > 0)
			cgs.addEffect(effect, modifiers);

		if (maxstat)
			effected.getLifeStats().synchronizeWithMaxStats();
	}

	/**
	 * @param effect
	 * @return
	 */
	protected List<IStatFunction> getModifiers(Effect effect) {
		int skillId = effect.getSkillId();
		int skillLvl = effect.getSkillLevel();

		List<IStatFunction> modifiers = new ArrayList<>();

		if (change == null)
			return modifiers;

		for (Change changeItem : change) {
			if (changeItem.getStat() == null) {
				LoggerFactory.getLogger(BufEffect.class).warn("Skill stat has wrong name for skillid: " + skillId);
				continue;
			}

			int valueWithDelta = changeItem.getValue() + changeItem.getDelta() * skillLvl;

			Conditions conditions = changeItem.getConditions();
			switch (changeItem.getFunc()) {
				case ADD:
					modifiers.add(new StatAddFunction(changeItem.getStat(), valueWithDelta, true).withConditions(conditions));
					break;
				case PERCENT:
					modifiers.add(new StatRateFunction(changeItem.getStat(), valueWithDelta, true).withConditions(conditions));
					break;
				case REPLACE:
					modifiers.add(new StatSetFunction(changeItem.getStat(), valueWithDelta).withConditions(conditions));
					break;
			}
		}
		return modifiers;
	}
}
