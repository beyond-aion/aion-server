package com.aionemu.gameserver.model.drop;

import java.util.Collection;
import java.util.Set;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author MrPoke
 */
public interface DropCalculator {

	int dropCalculator(Set<DropItem> result, int index, float dropModifier, Race race, Collection<Player> groupMembers);
}
