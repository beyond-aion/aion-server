package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeaponDualEffect")
public class WeaponDualEffect extends BufEffect {

	@Override
	public void startEffect(Effect effect) {
		if (effect.getEffected() instanceof Player p) {
			p.getGameStats().setSkillEfficiency(skillEfficiency / 100f);
			p.getGameStats().setMaxDamageChance(maxDamageChance + effect.getSkillLevel() * maxDamageDelta);
			p.getGameStats().setMinDamageRatio((value + effect.getSkillLevel() * delta) / 100f);
			p.getGameStats().updateStatsVisually();
		}
	}

	@Override
	public void endEffect(Effect effect) {
		if (effect.getEffected() instanceof Player p) {
			p.getGameStats().setSkillEfficiency(0);
			p.getGameStats().setMaxDamageChance(0);
			p.getGameStats().setMinDamageRatio(0);
			p.getGameStats().updateStatsVisually();
		}
		super.endEffect(effect);
	}
}
