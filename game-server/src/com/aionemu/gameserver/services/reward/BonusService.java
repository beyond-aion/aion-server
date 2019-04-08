package com.aionemu.gameserver.services.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Chance;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.itemgroups.BonusItemGroup;
import com.aionemu.gameserver.model.templates.itemgroups.ItemRaceEntry;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.templates.rewards.BonusType;

/**
 * @author Rolandas, Pad, Neon
 */
public class BonusService {

	private BonusService() {
	}

	public static QuestItems getQuestBonus(Player player, QuestTemplate questTemplate) {
		if (questTemplate.getBonus() == null)
			return null;

		List<? extends ItemRaceEntry> itemsOfRandomGroup = getMatchingItemsOfRandomGroup(player, questTemplate);
		if (itemsOfRandomGroup == null)
			return null;

		ItemRaceEntry item = Chance.selectElement(itemsOfRandomGroup);
		return item == null ? null : new QuestItems(item.getId(), item.getCount());
	}

	public static List<? extends ItemRaceEntry> getMatchingItemsOfRandomGroup(Player player, QuestTemplate questTemplate) {
		List<BonusItemGroup> remainingGroups = new ArrayList<>(getBonusGroups(questTemplate.getBonus().getType()));
		List<? extends ItemRaceEntry> allRewards = null;

		while (!remainingGroups.isEmpty()) {
			BonusItemGroup group = Chance.selectElement(remainingGroups, true);
			if (group == null)
				break;
			allRewards = group.getItems().stream().filter(i -> i.matches(player.getRace(), questTemplate)).collect(Collectors.toList());
			if (!allRewards.isEmpty())
				break;
		}
		return allRewards;
	}

	private static List<BonusItemGroup> getBonusGroups(BonusType type) {
		switch (type) {
			// case GATHER:
			// return DataManager.ITEM_GROUPS_DATA.getGatherGroups();
			// case BOSS:
			// return DataManager.ITEM_GROUPS_DATA.getBossGroups();
			// case ENCHANT:
			// return DataManager.ITEM_GROUPS_DATA.getEnchantGroups();
			case EVENTS:
				return DataManager.ITEM_GROUPS_DATA.getEventGroups();
			case FOOD:
				return DataManager.ITEM_GROUPS_DATA.getFoodGroups();
			case MANASTONE:
				return DataManager.ITEM_GROUPS_DATA.getManastoneGroups();
			case MEDICINE:
				return DataManager.ITEM_GROUPS_DATA.getMedicineGroups();
			case MEDAL:
				return DataManager.ITEM_GROUPS_DATA.getMedalGroups();
			case TASK:
				return DataManager.ITEM_GROUPS_DATA.getCraftGroups();
			case MOVIE:
			case NONE:
				break;
			default:
				LoggerFactory.getLogger(BonusService.class).warn("Bonus of type " + type + " is not implemented");
		}
		return Collections.emptyList();
	}
}
