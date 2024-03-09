package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.skillengine.SkillEngine;
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
	public void startEffect(final Effect effect) {
		effect.addObserver(effect.getEffected(), new ActionObserver(ObserverType.HP_CHANGED) {

			private final AtomicBoolean isHpBelowThreshold = new AtomicBoolean();

			@Override
			public void hpChanged(int hpValue) {
				if (hpValue <= (int) (value / 100f * effect.getEffected().getLifeStats().getMaxHp())) {
					if (!isHpBelowThreshold.getAndSet(true))
						SkillEngine.getInstance().applyEffectDirectly(skillId, effect.getEffected(), effect.getEffected());
				} else {
					isHpBelowThreshold.compareAndSet(true, false);
				}
			}
		});
	}

}
