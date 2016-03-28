package com.aionemu.gameserver.model.templates.item;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author antness
 * @modified Neon
 */
@XmlType(name = "ResultedItem")
public class ResultedItem {

	@XmlAttribute(name = "id")
	public int itemId;
	@XmlAttribute(name = "min_count")
	public int minCount = 1;
	@XmlAttribute(name = "max_count")
	public int maxCount;
	@XmlAttribute(name = "race")
	public Race race = Race.PC_ALL;
	@XmlList
	@XmlAttribute(name = "player_classes")
	public List<PlayerClass> playerClasses;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (maxCount > 0 && maxCount < minCount)
			LoggerFactory.getLogger(ResultedItem.class).warn("Wrong count for decomposable result item:{}, min:{} max:{}", itemId, minCount, maxCount);
	}

	public int getItemId() {
		return itemId;
	}

	public int getMinCount() {
		return minCount;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public final Race getRace() {
		return race;
	}

	public List<PlayerClass> getPlayerClasses() {
		return playerClasses;
	}

	public final int getResultCount() {
		return maxCount == 0 ? minCount : Rnd.get(minCount, maxCount);
	}

	/**
	 * @param player
	 * @return True if the specified player is allowed to acquire this item. False if any condition does not match.
	 */
	public boolean isObtainableFor(Player player) {
		return (playerClasses == null || playerClasses.contains(player.getPlayerClass()) || playerClasses.contains(PlayerClass.ALL))
			&& (race == Race.PC_ALL || race == player.getRace());
	}
}
