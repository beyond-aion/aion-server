package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HostileUpEffect")
public class HostileUpEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		Creature effected = effect.getEffected();
		if (effected instanceof Npc) {
			int totalHate = effect.getTauntHate();
			// FIXME some skills never broadcast regular hate. that's why the following check exists as a workaround, which should be removed once fixed
			// hate broadcasts in Effect.startEffect (if added to EffectController) and applyEffect (if there are no successEffects), so some never do
			if (effect.getSuccessEffects().size() == 1) // only this effect template is present, therefore we know regular hate will never broadcast
				totalHate += effect.getEffectHate();
			((Npc) effected).getAggroList().addHate(effect.getEffector(), totalHate);
		}
	}

	@Override
	public void calculate(Effect effect) {
		if (!super.calculate(effect, null, null))
			return;
		effect.setTauntHate(value + delta * effect.getSkillLevel());
	}
}
