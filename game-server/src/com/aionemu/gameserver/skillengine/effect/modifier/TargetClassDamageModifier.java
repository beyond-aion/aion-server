package com.aionemu.gameserver.skillengine.effect.modifier;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetClassDamageModifier")
public class TargetClassDamageModifier extends ActionModifier {

	@XmlAttribute(name = "class")
	private PlayerClass skillTargetClass;

	@Override
	public int analyze(Effect effect) {
		Creature effected = effect.getEffected();
		if (effected instanceof Player) {
			Player player = (Player) effected;
			if (player.getPlayerClass() == skillTargetClass) {
				return value + effect.getSkillLevel() * delta;
			}
		}
		return 0;
	}

	@Override
	public boolean check(Effect effect) {
		Creature effected = effect.getEffected();
		if (effected instanceof Player) {
			Player player = (Player) effected;
			return player.getPlayerClass() == skillTargetClass;
		}
		return false;
	}

}
