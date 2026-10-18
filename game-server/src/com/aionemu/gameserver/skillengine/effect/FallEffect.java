package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FallEffect")
public class FallEffect extends EffectTemplate {

	@Override
	protected boolean isDodgedOrResisted(Effect effect, StatEnum statEnum) {
		if (effect.getEffected().getEffectController().isInAnyAbnormalState(AbnormalState.INVULNERABLE_WING)) {
			return true;
		}
		return super.isDodgedOrResisted(effect, statEnum);
	}

	@Override
	public void applyEffect(Effect effect) {
		if (effect.getEffected() instanceof Player player) {
			player.getFlyController().endFly(true);
		}
	}
}
