package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.observer.AttackStatusObserver;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BlindEffect")
public class BlindEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		int visualStateExcludingBlinking = effect.getEffected().getVisualState() & ~CreatureVisualState.BLINKING.getId();
		if (visualStateExcludingBlinking < CreatureVisualState.HIDE10.getId())
			effect.getEffected().getEffectController().removeHideEffects();
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, StatEnum.BLIND_RESISTANCE, null);
	}

	@Override
	public void startEffect(Effect effect) {
		effect.setAbnormal(AbnormalState.BLIND);
		effect.getEffected().getEffectController().setAbnormal(AbnormalState.BLIND);
		effect.addObserver(effect.getEffected(), new AttackStatusObserver(value, AttackStatus.DODGE) {

			@Override
			public boolean checkAttackerStatus(AttackStatus status) {
				return Rnd.chance() < value;
			}

		});
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.BLIND);
	}

}
