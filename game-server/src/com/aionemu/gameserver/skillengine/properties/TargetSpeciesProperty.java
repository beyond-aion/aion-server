package com.aionemu.gameserver.skillengine.properties;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Luzien
 */
public class TargetSpeciesProperty {

	public static boolean set(Properties properties, Properties.ValidationResult result) {
		switch (properties.getTargetSpecies()) {
			case NPC -> result.getTargets().removeIf(effected -> !(effected instanceof Npc));
			case PC -> result.getTargets().removeIf(effected -> !(effected instanceof Player));
		}
		return true;
	}
}
