package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author Rama and Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BoostSkillCostEffect")
public class BoostSkillCostEffect extends BufEffect {

	@XmlAttribute
	protected boolean percent;

	@Override
	public void startEffect(final Effect effect) {
		super.startEffect(effect);

		effect.addObserver(effect.getEffected(), new ActionObserver(ObserverType.BOOSTSKILLCOST) {

			@Override
			public void boostSkillCost(Skill skill) {
				skill.setBoostSkillCost(value);
			}
		});
	}
}
