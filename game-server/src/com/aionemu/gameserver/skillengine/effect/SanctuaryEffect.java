package com.aionemu.gameserver.skillengine.effect;

import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author kecimis
 * @modified Cheatkiller, add AbnormalState
 */
public class SanctuaryEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
		if (effect.getEffector().equals(effect.getEffected()))
			effect.getEffected().setTarget(effect.getEffected());
	}

	@Override
	public void startEffect(Effect effect) {
		effect.setAbnormal(AbnormalState.SANCTUARY.getId());
		effect.getEffected().getEffectController().setAbnormal(AbnormalState.SANCTUARY.getId());
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.SANCTUARY.getId());
	}
}
