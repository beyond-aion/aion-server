package com.aionemu.gameserver.services.reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.ItemGroupsData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.itemgroups.BonusItemGroup;
import com.aionemu.gameserver.model.templates.itemgroups.CraftGroup;
import com.aionemu.gameserver.model.templates.itemgroups.ItemRaceEntry;
import com.aionemu.gameserver.model.templates.itemgroups.ManastoneGroup;
import com.aionemu.gameserver.model.templates.itemgroups.MedalGroup;
import com.aionemu.gameserver.model.templates.itemgroups.MedicineGroup;
import com.aionemu.gameserver.model.templates.quest.QuestBonuses;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.templates.rewards.BonusType;
import com.aionemu.gameserver.model.templates.rewards.CraftItem;
import com.aionemu.gameserver.model.templates.rewards.FullRewardItem;

import javolution.util.FastTable;

/**
 * @author Rolandas
 * @modified Pad
 */
public class BonusService {

	private static BonusService instance = new BonusService();
	private ItemGroupsData itemGroups = DataManager.ITEM_GROUPS_DATA;
	private static final Logger log = LoggerFactory.getLogger(BonusService.class);

	private BonusService() {
	}

	public static BonusService getInstance() {
		return instance;
	}

	public BonusItemGroup[] getGroupsByType(BonusType type) {
		switch (type) {
			case BOSS:
				return itemGroups.getBossGroups();
			case ENCHANT:
				return itemGroups.getEnchantGroups();
			case FOOD:
				return itemGroups.getFoodGroups();
			case GATHER:
				return ArrayUtils.addAll(itemGroups.getOreGroups(), itemGroups.getGatherGroups());
			case MANASTONE:
				return itemGroups.getManastoneGroups();
			case MEDICINE:
				return itemGroups.getMedicineGroups();
			case TASK:
				return itemGroups.getCraftGroups();
			case MOVIE:
				return null;
			default:
				log.warn("Bonus of type " + type + " is not implemented");
				return null;
		}
	}

	public BonusItemGroup getRandomGroup(BonusItemGroup[] groups) {
		float total = 0;

		if (groups == null)
			return null;

		for (BonusItemGroup gr : groups)
			total += gr.getChance();

		if (total == 0)
			return null;

		BonusItemGroup chosenGroup = null;
		int percent = 100;
		for (BonusItemGroup gr : groups) {
			float chance = getNormalizedChance(gr.getChance(), total);
			if (Rnd.get(0, percent) <= chance) {
				chosenGroup = gr;
				break;
			} else
				percent -= chance;
		}
		return chosenGroup;
	}

	private float getNormalizedChance(float chance, float total) {
		return chance * 100f / total;
	}

	public BonusItemGroup getRandomGroup(BonusType type) {
		return getRandomGroup(getGroupsByType(type));
	}

	public QuestItems getQuestBonus(Player player, QuestTemplate questTemplate) {
		List<QuestBonuses> bonuses = questTemplate.getBonus();

		if (bonuses.isEmpty())
			return null;

		QuestBonuses bonus = bonuses.get(0);
		if (bonus.getType() == BonusType.NONE)
			return null;

		switch (bonus.getType()) {
			case TASK:
				return getCraftBonus(player, questTemplate);
			case EVENT_LEGENDARY_SYMPHONY:
				return getEventLegendarySymphonyBonus(player, bonus);
			case MANASTONE:
				return getManastoneBonus(player, bonus);
			case MEDAL:
				return getMedalBonus(player, questTemplate);
			case MEDICINE:
				return getMedicineBonus(player, bonus);
			case MOVIE:
				return null;
			default:
				log.warn("Quest bonus of type " + bonus.getType() + " is not implemented (quest " + questTemplate.getId() + ")");
				return null;
		}
	}

	private QuestItems getCraftBonus(Player player, QuestTemplate questTemplate) {
		BonusItemGroup[] groups = itemGroups.getCraftGroups();
		CraftGroup group = null;
		ItemRaceEntry[] allRewards = null;

		while (groups != null && groups.length > 0 && group == null) {
			group = (CraftGroup) getRandomGroup(groups);
			if (group == null)
				break;
			allRewards = group.getRewards(questTemplate.getCombineSkill(), questTemplate.getCombineSkillPoint());
			if (allRewards.length == 0) {
				List<BonusItemGroup> temp = new FastTable<>();
				Collections.addAll(temp, groups);
				temp.remove(group);
				group = null;
				groups = temp.toArray(new BonusItemGroup[0]);
			}
		}

		if (group == null || allRewards == null)
			return null;

		List<ItemRaceEntry> finalList = new FastTable<>();
		for (ItemRaceEntry allReward : allRewards) {
			ItemRaceEntry r = allReward;
			ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(r.getId());

			if (template == null) {
				log.warn("Item " + r.getId() + " absent in ItemTemplate");
				continue;
			}
			if (!r.checkRace(player.getCommonData().getRace()))
				continue;

			finalList.add(r);
		}

		if (finalList.isEmpty())
			return null;

		int itemCount = 1;

		ItemRaceEntry reward = Rnd.get(finalList);
		if (reward instanceof CraftItem)
			itemCount = Rnd.get(3, 5);

		return new QuestItems(reward.getId(), itemCount);
	}

	private QuestItems getEventLegendarySymphonyBonus(Player player, QuestBonuses bonus) {
		List<FullRewardItem> possibleRewards = new ArrayList<>();

		float total = 0;
		for (FullRewardItem fRI : itemGroups.getEventLegendarySymphony().getItems()) {
			if (fRI.getLevel() != bonus.getLevel()) {
				continue;
			}
			total += fRI.getChance();
			possibleRewards.add(fRI);
		}

		if (total == 0)
			return null;

		float rnd = Rnd.get() * total;
		float luck = 0;

		for (FullRewardItem fRI : possibleRewards) {
			luck += fRI.getChance();

			if (rnd <= luck) {
				return new QuestItems(fRI.getId(), fRI.getCount());
			}
		}
		return null;
	}

	private QuestItems getMedalBonus(Player player, QuestTemplate template) {
		BonusItemGroup[] groups = itemGroups.getMedalGroups();
		MedalGroup group = (MedalGroup) getRandomGroup(groups);
		FullRewardItem finalReward = null;
		int bonusLevel = template.getBonus().get(0).getLevel();
		float total = 0;

		for (FullRewardItem medal : group.getItems()) {
			if (medal.getLevel() == bonusLevel)
				total += medal.getChance();
		}

		if (total == 0)
			return null;

		float rnd = Rnd.get() * total;
		float luck = 0;
		for (FullRewardItem medal : group.getItems()) {
			if (medal.getLevel() != bonusLevel)
				continue;

			luck += medal.getChance();
			if (rnd <= luck) {
				finalReward = medal;
				break;
			}
		}
		return finalReward != null ? new QuestItems(finalReward.getId(), finalReward.getCount()) : null;
	}

	private QuestItems getManastoneBonus(Player player, QuestBonuses bonus) {
		ManastoneGroup group = (ManastoneGroup) getRandomGroup(BonusType.MANASTONE);
		ItemRaceEntry[] allRewards = group.getRewards();
		List<ItemRaceEntry> finalList = new FastTable<>();

		for (ItemRaceEntry r : allRewards) {
			ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(r.getId());

			if (bonus.getLevel() != template.getLevel())
				continue;

			finalList.add(r);
		}

		if (finalList.isEmpty())
			return null;

		ItemRaceEntry reward = Rnd.get(finalList);
		return new QuestItems(reward.getId(), 1);
	}

	private QuestItems getMedicineBonus(Player player, QuestBonuses bonus) {
		MedicineGroup group = (MedicineGroup) getRandomGroup(BonusType.MEDICINE);
		ItemRaceEntry[] allRewards = group.getRewards();
		List<ItemRaceEntry> finalList = new FastTable<>();

		for (ItemRaceEntry r : allRewards) {
			ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(r.getId());

			if (bonus.getLevel() != template.getLevel())
				continue;

			finalList.add(r);
		}

		if (finalList.isEmpty())
			return null;

		ItemRaceEntry reward = Rnd.get(finalList);
		return new QuestItems(reward.getId(), 1);
	}
}
