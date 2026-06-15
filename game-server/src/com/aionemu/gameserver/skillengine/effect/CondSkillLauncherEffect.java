package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.ActivationAttribute;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HealType;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CondSkillLauncherEffect")
public class CondSkillLauncherEffect extends EffectTemplate {

	@XmlAttribute(name = "skill_id")
	protected int skillId;
	@XmlAttribute
	protected HealType type;

	// TODO what if you fall? effect is not applied? what if you use skill that consume hp?
	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(Effect effect) {
		effect.addObserver(effect.getEffected(), new ActionObserver(ObserverType.HP_CHANGED) {

			private Effect conditionalEffect;

			@Override
			public void hpChanged(int hpValue) {
				boolean hpAtOrBelowThreshold = hpValue <= value * effect.getEffected().getLifeStats().getMaxHp() / 100;
				synchronized (this) {
					if (hpAtOrBelowThreshold && conditionalEffect == null) {
						boolean permanent = effect.getSkillTemplate().getActivationAttribute() == ActivationAttribute.PASSIVE;
						Integer duration = permanent ? 0 : null; // passive skills like Determination have no time limit
						conditionalEffect = SkillEngine.getInstance().applyEffectDirectly(skillId, effect.getEffected(), effect.getEffected(), duration, null);
					} else if (!hpAtOrBelowThreshold && conditionalEffect != null) {
						conditionalEffect.endEffect();
						conditionalEffect = null;
					}
				}
			}

			@Override
			public void onRemoved() {
				if (conditionalEffect != null)
					conditionalEffect.endEffect();
			}
		});
	}

}
