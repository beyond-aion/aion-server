package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetrificationEffect")
public class PetrificationEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, StatEnum.PERIFICATION_RESISTANCE, null);
	}

	@Override
	public void startEffect(Effect effect) {
		Creature effected = effect.getEffected();
		effected.getMoveController().abortMove();
		effected.getController().cancelCurrentSkill(effect.getEffector());
		// removes glide
		if (effected instanceof Player && ((Player) effected).isInGlidingState()) {
			((Player) effected).getFlyController().onStopGliding();
		}
		effect.getEffected().getEffectController().setAbnormal(AbnormalState.PETRIFICATION);
		effect.setAbnormal(AbnormalState.PETRIFICATION);
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.PETRIFICATION);
	}

}
