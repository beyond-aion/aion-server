package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Bobobear
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetOrderUnSummonEffect")
public class PetOrderUnSummonEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		Creature effected = effect.getEffected();
		if (effected instanceof Player) {
			Summon summon = ((Player) effected).getSummon();
			if (summon != null) {
				SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.UNSPECIFIED);
			}
		}
	}
}
