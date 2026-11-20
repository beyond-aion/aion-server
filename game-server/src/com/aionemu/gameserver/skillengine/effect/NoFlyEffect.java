package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NoFlyEffect")
public class NoFlyEffect extends EffectTemplate {

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, StatEnum.NOFLY_RESISTANCE, null);
	}

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	protected boolean isDodgedOrResisted(Effect effect, StatEnum statEnum) {
		return hasInvulnerableWings(effect.getEffected()) || super.isDodgedOrResisted(effect, statEnum);
	}

	@Override
	public void startEffect(Effect effect) {
		Creature effected = effect.getEffected();
		Player player = getPlayerOrSummonMaster(effected);
		if (player == null)
			return;
		effect.setAbnormal(AbnormalState.NOFLY);
		effected.getEffectController().addNoFlySource(effect);
		if (player != effected)
			player.getEffectController().addNoFlySource(effect);
		player.getFlyController().forceEndFlight();
		if (player != effected)
			player.getEffectController().refreshAbnormalStatus();
	}

	@Override
	public void endEffect(Effect effect) {
		Creature effected = effect.getEffected();
		effected.getEffectController().removeNoFlySource(effect);
		Player player = getPlayerOrSummonMaster(effected);
		if (player != null && player != effected && player.getEffectController().removeNoFlySource(effect))
			player.getEffectController().refreshAbnormalStatus();
	}
}
