package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AggroList;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Luzien
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SwitchHostileEffect")
public class SwitchHostileEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {

		Creature effected = effect.getEffected();
		Creature effector = effect.getEffector();
		AggroList aggroList = effected.getAggroList();

		if (((Player) effector).getSummon() != null) {
			Creature summon = ((Player) effector).getSummon();
			int playerHate = aggroList.getAggroInfo(effector).getHate();
			int summonHate = aggroList.getAggroInfo(((Player) effector).getSummon()).getHate();

			aggroList.stopHating(summon);
			aggroList.stopHating(effector);
			aggroList.addHate(effector, summonHate);
			aggroList.addHate(summon, playerHate);
		}
	}
}
