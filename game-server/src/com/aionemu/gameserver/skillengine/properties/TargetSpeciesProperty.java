package com.aionemu.gameserver.skillengine.properties;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author Luzien
 */
public class TargetSpeciesProperty {

	/**
	 * @param skill
	 * @param properties
	 * @return
	 */
	public static boolean set(final Skill skill, Properties properties) {
		switch (properties.getTargetSpecies()) {
			case NPC:
				skill.getEffectedList().removeIf(effected -> !(effected instanceof Npc));
				break;
			case PC:
				skill.getEffectedList().removeIf(effected -> !(effected instanceof Player));
				break;
		}
		return true;
	}
}
