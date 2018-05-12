package com.aionemu.gameserver.services.drop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.GlobalNpcExclusionData;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.drop.NpcDrop;
import com.aionemu.gameserver.model.gameobjects.DropNpc;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropItem;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropMap;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropNpc;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropNpcGroup;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropRace;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropRating;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropTribe;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropWorld;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropZone;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalRule;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_STATUS;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.DropRewardEnum;
import com.aionemu.gameserver.world.WorldDropType;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author xTz, Aioncool, Bobobear
 * @modified Neon
 */
public class DropRegistrationService {

	private Map<Integer, Set<DropItem>> currentDropMap = new ConcurrentHashMap<>();
	private Map<Integer, DropNpc> dropRegistrationMap = new ConcurrentHashMap<>();

	private DropRegistrationService() {
	}

	public void registerDrop(Npc npc, Player player, Collection<Player> groupMembers) {
		registerDrop(npc, player, player.getLevel(), groupMembers);
	}

	/**
	 * After NPC dies, it can register arbitrary drop
	 */
	public void registerDrop(Npc npc, Player player, int highestLevel, Collection<Player> groupMembers) {
		int npcObjId = npc.getObjectId();

		// Getting all possible drops for this Npc
		NpcDrop npcDrop = DataManager.CUSTOM_NPC_DROP.getNpcDrop(npc.getNpcId());
		String dropType = npc.getGroupDrop().name().toLowerCase();
		boolean isChest = npc.getAi().getName().equals("chest") || dropType.startsWith("treasure") || dropType.endsWith("box");

		List<Player> allowedLooters = new ArrayList<>();
		Player looter = player;
		int winnerObj = 0;
		Player teamLooter = initDropNpc(player, npcObjId, allowedLooters, groupMembers);
		if (teamLooter != null) {
			looter = teamLooter;
			winnerObj = teamLooter.getObjectId();
		}

		int index = 1;
		Set<DropItem> droppedItems = new HashSet<>();
		float dropRate = calculateDropRate(looter, npc, isChest, highestLevel);

		if (npcDrop != null) // add custom drops
			index = npcDrop.dropCalculator(droppedItems, index, dropRate, looter.getRace(), groupMembers);

		// Updating current dropMap
		currentDropMap.put(npcObjId, droppedItems);

		index = QuestService.getQuestDrop(droppedItems, index, npc, groupMembers, looter);

		// if npc ai == quest_use_item it will be always excluded from global drops
		boolean isNpcQuest = npc.getAi().getName().equals("quest_use_item");
		if (!isNpcQuest && !hasGlobalNpcExclusions(npc)) {
			boolean isAllowedDefaultGlobalDropNpc = isAllowedDefaultGlobalDropNpc(npc, isChest);
			// instances with WorldDropType.NONE must not have global drops (example Arenas)
			if (npc.getWorldDropType() != WorldDropType.NONE) {
				index = addGlobalDrops(index, dropRate, looter, npc, isAllowedDefaultGlobalDropNpc, DataManager.GLOBAL_DROP_DATA.getAllRules(), droppedItems,
					groupMembers, winnerObj);
			}
			addGlobalDrops(index, dropRate, looter, npc, isAllowedDefaultGlobalDropNpc, EventService.getInstance().getActiveEventDropRules(), droppedItems,
				groupMembers, winnerObj);
		}

		if (npc.isInInstance()) {
			npc.getPosition().getWorldMapInstance().getInstanceHandler().onDropRegistered(npc);
		}
		npc.getAi().onGeneralEvent(AIEventType.DROP_REGISTERED);

		for (Player p : allowedLooters) {
			PacketSendUtility.sendPacket(p, new SM_LOOT_STATUS(npcObjId, 0));
		}

		DropService.getInstance().scheduleFreeForAll(npcObjId);
	}

	private Player initDropNpc(Player player, int npcObjId, List<Player> allowedLooters, Collection<Player> groupMembers) {
		Player looter = null;
		DropNpc dropNpc = new DropNpc(npcObjId);
		// Distributing drops to players
		if (player.isInGroup() || player.isInAlliance()) {
			LootGroupRules lootGrouRules = player.getLootGroupRules();

			switch (lootGrouRules.getLootRule()) {
				case ROUNDROBIN:
					int size = groupMembers.size();
					if (size > lootGrouRules.getNrRoundRobin())
						lootGrouRules.setNrRoundRobin(lootGrouRules.getNrRoundRobin() + 1);
					else
						lootGrouRules.setNrRoundRobin(1);

					int i = 0;
					for (Player p : groupMembers) {
						i++;
						if (i == lootGrouRules.getNrRoundRobin()) {
							allowedLooters.add(p);
							looter = p;
							break;
						}
					}
					break;
				case FREEFORALL:
					allowedLooters.addAll(groupMembers);
					break;
				case LEADER:
					Player leader = player.isInGroup() ? player.getPlayerGroup().getLeaderObject() : player.getPlayerAlliance().getLeaderObject();
					allowedLooters.add(leader);
					looter = leader;
					break;
			}

			dropNpc.setInRangePlayers(groupMembers);
			dropNpc.setGroupSize(groupMembers.size());
		} else {
			allowedLooters.add(player);
		}
		allowedLooters.forEach(dropNpc::setAllowedLooter);
		dropRegistrationMap.put(npcObjId, dropNpc);
		return looter;
	}

	public boolean isAllowedDefaultGlobalDropNpc(Npc npc, boolean isChest) {
		// exclude most siege spawns, and inner base spawns
		if (npc.getSpawn() instanceof SiegeSpawnTemplate && npc.getAbyssNpcType() != AbyssNpcType.DEFENDER)
			return false;
		if (npc.getSpawn() instanceof BaseSpawnTemplate && npc.getSpawn().getHandlerType() != SpawnHandlerType.OUTRIDER
			&& npc.getSpawn().getHandlerType() != SpawnHandlerType.OUTRIDER_ENHANCED)
			return false;
		// if npc level == 1 means missing stats, so better exclude it from drops
		if (npc.getLevel() < 2 && !isChest && npc.getWorldId() != WorldMapType.POETA.getId() && npc.getWorldId() != WorldMapType.ISHALGEN.getId())
			return false;
		// if abyss type npc != null or npc is chest, the npc will be excluded from drops
		if (isChest || npc.getAbyssNpcType() != AbyssNpcType.NONE && npc.getAbyssNpcType() != AbyssNpcType.DEFENDER)
			return false;
		return true;
	}

	private int addGlobalDrops(int index, float dropRate, Player player, Npc npc, boolean isAllowedDefaultGlobalDropNpc, List<GlobalRule> rules,
		Set<DropItem> droppedItems, Collection<Player> groupMembers, int winnerObj) {
		for (GlobalRule rule : rules) {
			// if getGlobalRuleNpcs() != null means drops are for specified npcs (like named drops) so the default restrictions will be ignored
			if (isAllowedDefaultGlobalDropNpc || rule.getGlobalRuleNpcs() != null) {
				float chance = calculateEffectiveChance(rule, npc, dropRate);
				if (Rnd.chance() >= chance)
					continue;

				index = addDropItems(index, droppedItems, rule, npc, player, groupMembers, winnerObj);
			}
		}
		return index;
	}

	public float calculateDropRate(Player player, Npc npc, boolean isChest, int highestLevel) {
		int dropChance = 100;
		if (!DropConfig.DISABLE_REDUCTION && (!isChest || npc.getLevel() != 1) && !DropConfig.NO_REDUCTION_MAPS.contains(npc.getWorldId()))
			dropChance = DropRewardEnum.dropRewardFrom(npc.getLevel() - highestLevel); // reduce chance depending on level
		float boostDropRate = calculateBoostDropRate(player, npc);
		float dropRate = Rates.get(player, RatesConfig.DROP_RATES) * boostDropRate * dropChance / 100F;
		return dropRate;
	}

	private float calculateBoostDropRate(Player genesis, Npc npc) {
		// Drop rate from NPC can be boosted by Spiritmaster Erosion skill
		float boostDropRate = npc.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f;

		// Drop rate can be boosted by player buff too
		boostDropRate += genesis.getGameStats().getStat(StatEnum.DR_BOOST, 0).getCurrent() / 100f;

		// Some personal drop boost
		// EoR 5% Boost drop rate
		boostDropRate += genesis.getCommonData().getCurrentReposeEnergy() > 0 ? 0.05f : 0;
		// EoS 5% Boost drop rate
		boostDropRate += genesis.getCommonData().getCurrentSalvationPercent() > 0 ? 0.05f : 0;
		// Deed to Palace 5% Boost drop rate
		boostDropRate += genesis.getActiveHouse() != null && genesis.getActiveHouse().getHouseType() == HouseType.PALACE ? 0.05f : 0;
		// Hmm.. 169625013 have boost drop rate 5% info but no such desc on buff

		// can be exploited on duel with Spiritmaster Erosion skill
		boostDropRate += genesis.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f - 1;
		return boostDropRate;
	}

	public float calculateEffectiveChance(GlobalRule rule, Npc npc, float dropRate) {
		float chance = rule.getChance();
		// if fixed_chance == true means all mobs will have the same base chance (npcRating and npcRank will be excluded from calculation)
		if (!rule.isFixedChance())
			chance *= getRankModifier(npc) * getRatingModifier(npc);
		// ignore chance reducing dropRate if it's a noReduction rule
		if (dropRate > 1 || !rule.getNoReduction())
			chance *= dropRate;
		return chance;
	}

	private int addDropItems(int index, Set<DropItem> droppedItems, GlobalRule rule, Npc npc, Player player, Collection<Player> groupMembers,
		int winnerObj) {
		List<GlobalDropItem> alloweditems = getAllowedItems(rule, npc, player);
		if (!alloweditems.isEmpty()) {
			if (rule.getMemberLimit() > 1 && player.isInTeam()) {
				List<Player> members = new ArrayList<>(groupMembers);
				if (rule.getMemberLimit() > members.size())
					Collections.shuffle(members);
				int distributedItems = 0;
				for (Player member : members) {
					for (GlobalDropItem itemListed : alloweditems) {
						DropItem dropitem = new DropItem(new Drop(itemListed.getId(), 1, 1, 100, false));
						dropitem.setCount(getItemCount(itemListed.getId(), rule, npc));
						dropitem.setIndex(index++);
						dropitem.setPlayerObjId(member.getObjectId());
						dropitem.setWinningPlayer(member);
						dropitem.isDistributeItem(true);
						droppedItems.add(dropitem);
					}
					if (++distributedItems >= rule.getMemberLimit())
						break;
				}
			} else {
				for (GlobalDropItem itemListed : alloweditems) {
					droppedItems.add(regDropItem(index++, winnerObj, npc.getObjectId(), itemListed.getId(), getItemCount(itemListed.getId(), rule, npc)));
				}
			}
		}
		return index;
	}

	public DropItem regDropItem(int index, int playerObjId, int objId, int itemId, long count) {
		DropItem item = new DropItem(new Drop(itemId, 1, 1, 100, false));
		item.setPlayerObjId(playerObjId);
		item.setNpcObj(objId);
		item.setCount(count);
		item.setIndex(index);
		return item;
	}

	/**
	 * @return dropRegistrationMap
	 */
	public Map<Integer, DropNpc> getDropRegistrationMap() {
		return dropRegistrationMap;
	}

	/**
	 * @return currentDropMap
	 */
	public Map<Integer, Set<DropItem>> getCurrentDropMap() {
		return currentDropMap;
	}

	public static DropRegistrationService getInstance() {
		return SingletonHolder.instance;
	}

	public boolean hasGlobalNpcExclusions(Npc npc) {
		GlobalNpcExclusionData gde = DataManager.GLOBAL_EXCLUSION_DATA;
		if (!gde.isEmpty()) {
			if (gde.getNpcIds().contains(npc.getNpcId()) || gde.getNpcNames().contains(npc.getName())
				|| gde.getNpcTemplateTypes().contains(npc.getNpcTemplateType()) || npc.getTribe() != null && gde.getNpcTribes().contains(npc.getTribe())
				|| gde.getNpcAbyssTypes().contains(npc.getAbyssNpcType()))
				return true;
		}
		return false;
	}

	private boolean checkRuleRestrictions(GlobalRule rule, Race race, Npc npc) {
		if (!checkRestrictionRace(rule, race))
			return false;
		if (!checkGlobalRuleMaps(rule, npc))
			return false;
		if (!checkGlobalRuleWorlds(rule, npc))
			return false;
		if (!checkGlobalRuleRatings(rule, npc))
			return false;
		if (!checkGlobalRuleRaces(rule, npc))
			return false;
		if (!checkGlobalRuleTribes(rule, npc))
			return false;
		if (!checkGlobalRuleZones(rule, npc))
			return false;
		if (!checkGlobalRuleNpcs(rule, npc))
			return false;
		if (!checkGlobalRuleNpcGroups(rule, npc)) // drop group from npc_templates
			return false;
		if (!checkGlobalRuleExcludedNpcs(rule, npc))
			return false;
		return true;
	}

	private boolean checkRestrictionRace(GlobalRule rule, Race race) {
		if (rule.getRestrictionRace() != null) {
			if (race == Race.ASMODIANS && rule.getRestrictionRace() == GlobalRule.RestrictionRace.ELYOS
				|| race == Race.ELYOS && rule.getRestrictionRace() == GlobalRule.RestrictionRace.ASMODIANS)
				return false;
		}
		return true;
	}

	private boolean checkGlobalRuleMaps(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleMaps() != null) {
			for (GlobalDropMap gdMap : rule.getGlobalRuleMaps().getGlobalDropMaps())
				if (gdMap.getMapId() == npc.getPosition().getMapId())
					return true;
			return false;
		}
		return true;
	}

	private boolean checkGlobalRuleWorlds(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleWorlds() != null) {
			for (GlobalDropWorld gdWorld : rule.getGlobalRuleWorlds().getGlobalDropWorlds())
				if (gdWorld.getWorldDropType().equals(npc.getWorldDropType()))
					return true;
			return false;
		}
		return true;
	}

	private boolean checkGlobalRuleRatings(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleRatings() != null) {
			for (GlobalDropRating gdRating : rule.getGlobalRuleRatings().getGlobalDropRatings())
				if (gdRating.getRating().equals(npc.getRating()))
					return true;
			return false;
		}
		return true;
	}

	private boolean checkGlobalRuleRaces(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleRaces() != null) {
			for (GlobalDropRace gdRace : rule.getGlobalRuleRaces().getGlobalDropRaces())
				if (gdRace.getRace().equals(npc.getRace()))
					return true;
			return false;
		}
		return true;
	}

	private boolean checkGlobalRuleTribes(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleTribes() != null) {
			for (GlobalDropTribe gdTribe : rule.getGlobalRuleTribes().getGlobalDropTribes())
				if (gdTribe.getTribe().equals(npc.getTribe()))
					return true;
			return false;
		}
		return true;
	}

	private boolean checkGlobalRuleZones(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleZones() != null) {
			for (GlobalDropZone gdZone : rule.getGlobalRuleZones().getGlobalDropZones())
				if (npc.isInsideZone(ZoneName.get(gdZone.getZone())))
					return true;
			return false;
		}
		return true;
	}

	private boolean checkGlobalRuleNpcs(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleNpcs() != null) {
			for (GlobalDropNpc gdNpc : rule.getGlobalRuleNpcs().getGlobalDropNpcs())
				if (gdNpc.getNpcId() == npc.getNpcId())
					return true;
			return false;
		}
		return true;
	}

	private boolean checkGlobalRuleNpcGroups(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleNpcGroups() != null) {
			for (GlobalDropNpcGroup gdGroup : rule.getGlobalRuleNpcGroups().getGlobalDropNpcGroups())
				if (gdGroup.getGroup().equals(npc.getGroupDrop()))
					return true;
			return false;
		}
		return true;
	}

	private boolean checkGlobalRuleExcludedNpcs(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleExcludedNpcs() != null)
			return !rule.getGlobalRuleExcludedNpcs().getNpcIds().contains(npc.getNpcId());
		return true;
	}

	public List<GlobalDropItem> getAllowedItems(GlobalRule rule, Npc npc, Player player) {
		if (!checkRuleRestrictions(rule, player.getRace(), npc))
			return Collections.emptyList();
		List<GlobalDropItem> tempItems = new ArrayList<>();
		List<GlobalDropItem> allowedItems = new ArrayList<>();
		for (GlobalDropItem globalItem : rule.getDropItems()) {
			ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(globalItem.getId());
			if (player.getOppositeRace() == itemTemplate.getRace()) {
				continue;
			}
			int diff = npc.getLevel() - itemTemplate.getLevel();
			if (diff >= rule.getMinDiff() && diff <= rule.getMaxDiff())
				tempItems.add(globalItem);
		}
		if (tempItems.size() >= 1) {
			for (int i = 0; i < rule.getMaxDropRule(); i++) { // TODO: Evaluate if necessary
				float sumOfChances = calculateSumOfChances(tempItems);
				float currentSum = 0f;
				float rnd = Rnd.get((int) (sumOfChances * 1000)) / 1000f;
				for (Iterator<GlobalDropItem> iter = tempItems.iterator(); iter.hasNext();) {
					GlobalDropItem item = iter.next();
					currentSum += item.getChance();
					if (rnd < currentSum) {
						allowedItems.add(item);
						iter.remove();
						break;
					}
				}
			}
		}
		return allowedItems;
	}

	private float calculateSumOfChances(List<GlobalDropItem> items) {
		float sum = 0f;
		for (GlobalDropItem item : items)
			sum += item.getChance();
		return sum;
	}

	public long getItemCount(int itemId, GlobalRule rule, Npc npc) {
		long count = rule.getMaxCount() > 1 ? Rnd.get((int) rule.getMinCount(), (int) rule.getMaxCount()) : rule.getMinCount();
		if (itemId == 182400001)
			count *= npc.getLevel() * Math.pow(getRankModifier(npc) * getRatingModifier(npc), 6);
		return count;
	}

	public float getRankModifier(Npc npc) {
		switch (npc.getRank()) {
			case NOVICE:
				return 0.9f;
			case DISCIPLINED:
				return 1f;
			case SEASONED:
				return 1.05f;
			case EXPERT:
				return 1.1f;
			case VETERAN:
				return 1.15f;
			case MASTER:
				return 1.2f;
		}
		return 1f;
	}

	public float getRatingModifier(Npc npc) {
		switch (npc.getRating()) {
			case JUNK:
				return 0.5f;
			case NORMAL:
				return 1f;
			case ELITE:
				return 1.3f;
			case HERO:
				return 1.8f;
			case LEGENDARY:
				return 2f;
		}
		return 1f;
	}

	private static class SingletonHolder {

		protected static final DropRegistrationService instance = new DropRegistrationService();
	}

}
