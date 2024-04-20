package com.aionemu.gameserver.model.templates.item;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.ItemData;
import com.aionemu.gameserver.dataholders.StaticData;
import com.aionemu.gameserver.dataholders.loadingutils.StaticDataListener;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author antness, Neon
 */
@XmlType(name = "ResultedItem")
public class ResultedItem {

	@XmlAttribute(name = "id")
	private int itemId;
	@XmlAttribute(name = "min_count")
	private int minCount = 1;
	@XmlAttribute(name = "max_count")
	private int maxCount;
	@XmlAttribute(name = "race")
	private Race race = Race.PC_ALL;
	@XmlList
	@XmlAttribute(name = "player_classes")
	private List<PlayerClass> playerClasses;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		StaticData staticData = StaticDataListener.get(u);
		ItemData itemData = staticData != null ? staticData.itemData : DataManager.ITEM_DATA;
		if (itemData.getItemTemplate(itemId) == null)
			throw new IllegalArgumentException("Decomposable reward item ID is invalid: " + itemId);
		if (minCount <= 0)
			throw new IllegalArgumentException("Decomposable reward item [" + itemId + "] min_count (" + minCount + ") must be greater than 0");
		if (maxCount == 0)
			maxCount = minCount;
		else if (maxCount < minCount)
			throw new IllegalArgumentException(
					"Decomposable reward item [" + itemId + "] max_count (" + maxCount + ") must be unset or greater than min_count (" + minCount + ")");
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

	public boolean isObtainableFor(Player player) {
		return (playerClasses == null || playerClasses.contains(player.getPlayerClass()))
			&& (race == Race.PC_ALL || race == player.getRace());
	}
}
