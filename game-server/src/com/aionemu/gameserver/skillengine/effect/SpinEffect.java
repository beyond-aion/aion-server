package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SpellStatus;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpinEffect")
public class SpinEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect) {
		if (effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.PULLED)
			|| effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.SPIN))
			return;
		super.calculate(effect, StatEnum.SPIN_RESISTANCE, SpellStatus.SPIN);
	}

	@Override
	public void startEffect(Effect effect) {
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill();
		if (effected instanceof Player)
			((Player) effected).getFlyController().onStopGliding();
		effect.getEffected().getEffectController().removeParalyzeEffects();
		effected.getEffectController().setAbnormal(AbnormalState.SPIN.getId());
		effect.setAbnormal(AbnormalState.SPIN.getId());
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.SPIN.getId());
	}
}
