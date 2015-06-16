package com.aionemu.gameserver.skillengine.properties;


import java.util.Iterator;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Creature;
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
		TargetSpeciesAttribute value = properties.getTargetSpecies();

		final List<Creature> effectedList = skill.getEffectedList();

		switch (value) {
			case NPC:
				for (Iterator<Creature> iter = effectedList.iterator(); iter.hasNext();) {
					Creature nextEffected = iter.next();

					if (nextEffected instanceof Npc)
						continue;

					iter.remove();
				}
				break;
			case PC:
				for (Iterator<Creature> iter = effectedList.iterator(); iter.hasNext();) {
					Creature nextEffected = iter.next();

					if (nextEffected instanceof Player)
						continue;

					iter.remove();
				}
				break;
		}
		return true;
	}
}
