package com.aionemu.gameserver.services.drop;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.GlobalNpcExclusionData;
import com.aionemu.gameserver.model.Chance;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.drop.DropModifiers;
import com.aionemu.gameserver.model.drop.NpcDrop;
import com.aionemu.gameserver.model.gameobjects.DropNpc;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.templates.globaldrops.*;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_STATUS.Status;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.DropRewardEnum;
import com.aionemu.gameserver.world.WorldDropType;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author xTz, Aioncool, Bobobear, Neon
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
		DropModifiers dropModifiers = createDropModifiers(npc, looter, highestLevel);

		if (npcDrop != null) // add custom drops
			index = npcDrop.dropCalculator(droppedItems, index, dropModifiers, groupMembers);

		// Updating current dropMap
		currentDropMap.put(npcObjId, droppedItems);

		index = QuestService.getQuestDrop(droppedItems, index, npc, groupMembers, looter);

		// if npc ai == quest_use_item it will be always excluded from global drops
		boolean isNpcQuest = npc.getAi().getName().equals("quest_use_item");
		if (!isNpcQuest) {
			boolean hasGlobalNpcExclusions = hasGlobalNpcExclusions(npc);
			boolean isAllowedDefaultGlobalDropNpc = isAllowedDefaultGlobalDropNpc(npc, dropModifiers.isDropNpcChest());
			// instances with WorldDropType.NONE must not have global drops (example Arenas)
			if (!hasGlobalNpcExclusions && npc.getWorldDropType() != WorldDropType.NONE) {
				index = addGlobalDrops(index, dropModifiers, looter, npc, isAllowedDefaultGlobalDropNpc, DataManager.GLOBAL_DROP_DATA.getAllRules(),
					droppedItems, groupMembers, winnerObj);
			}
			if (!hasGlobalNpcExclusions || dropModifiers.isDropNpcChest())
				addGlobalDrops(index, dropModifiers, looter, npc, isAllowedDefaultGlobalDropNpc, EventService.getInstance().getActiveEventDropRules(),
					droppedItems, groupMembers, winnerObj);
		}

		npc.getPosition().getWorldMapInstance().getInstanceHandler().onDropRegistered(npc, winnerObj);
		npc.getAi().onGeneralEvent(AIEventType.DROP_REGISTERED);

		for (Player p : allowedLooters) {
			PacketSendUtility.sendPacket(p, new SM_LOOT_STATUS(npcObjId, Status.LOOT_ENABLE));
		}

		DropService.getInstance().scheduleFreeForAll(npcObjId);
	}

	public DropModifiers createDropModifiers(Npc npc, Player player, int highestLevel) {
		DropModifiers dropModifiers = new DropModifiers();
		String dropType = npc.getGroupDrop().name().toLowerCase();
		boolean isChest = npc.getAi().getName().equals("chest") || dropType.startsWith("treasure") || dropType.endsWith("box");
		dropModifiers.setIsDropNpcChest(isChest);
		dropModifiers.setDropRace(player.getRace());
		dropModifiers.setBoostDropRate(calculateBoostDropRate(player, npc));
		dropModifiers.setReductionDropRate(getReductionDropRate(npc, highestLevel));
		return dropModifiers;
	}

	private Player initDropNpc(Player player, int npcObjId, List<Player> allowedLooters, Collection<Player> groupMembers) {
		Player looter = null;
		DropNpc dropNpc = new DropNpc(npcObjId);
		// Distributing drops to players
		var lootingTeam = player.getCurrentTeam();
		if (lootingTeam != null) {
			LootGroupRules lootGroupRules = lootingTeam.getLootGroupRules();

			switch (lootGroupRules.getLootRule()) {
				case ROUNDROBIN:
					int size = groupMembers.size();
					if (size > lootGroupRules.getNrRoundRobin())
						lootGroupRules.setNrRoundRobin(lootGroupRules.getNrRoundRobin() + 1);
					else
						lootGroupRules.setNrRoundRobin(1);

					int i = 0;
					for (Player p : groupMembers) {
						i++;
						if (i == lootGroupRules.getNrRoundRobin()) {
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
			dropNpc.setLootingTeam(lootingTeam);
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

	private int addGlobalDrops(int index, DropModifiers dropModifiers, Player player, Npc npc, boolean isAllowedDefaultGlobalDropNpc,
		List<GlobalRule> rules, Set<DropItem> droppedItems, Collection<Player> groupMembers, int winnerObj) {
		for (GlobalRule rule : rules) {
			// if getGlobalRuleNpcs() != null means drops are for specified npcs (like named drops) so the default restrictions will be ignored
			if (isAllowedDefaultGlobalDropNpc || rule.getGlobalRuleNpcs() != null) {
				float chance = calculateEffectiveChance(rule, npc, dropModifiers);
				if (Rnd.chance() >= chance)
					continue;

				index = addDropItems(index, droppedItems, rule, npc, player, groupMembers, winnerObj, dropModifiers);
			}
		}
		return index;
	}

	private Float getReductionDropRate(Npc npc, int highestLevel) {
		int dropChance = DropRewardEnum.dropRewardFrom(npc.getLevel() - highestLevel); // reduced chance depending on level
		return dropChance == 100 ? null : dropChance / 100f;
	}

	private float calculateBoostDropRate(Player killer, Npc npc) {
		// Drop rate from NPC can be boosted by Spiritmaster Erosion skill
		int boostDropRate = npc.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent();
		// can be exploited on duel with Spiritmaster Erosion skill
		boostDropRate = killer.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, boostDropRate).getCurrent();
		// Drop rate can be boosted by player buff too
		boostDropRate = killer.getGameStats().getStat(StatEnum.DR_BOOST, boostDropRate).getCurrent();

		if (killer.getCommonData().getCurrentReposeEnergy() > 0) // EoR 5% Boost drop rate
			boostDropRate += 5;
		if (killer.getCommonData().getCurrentSalvationPercent() > 0) // EoS 5% Boost drop rate
			boostDropRate += 5;
		if (killer.getActiveHouse() != null && killer.getActiveHouse().getHouseType() == HouseType.PALACE) // Deed to Palace 5% Boost drop rate
			boostDropRate += 5;

		return Rates.get(killer, RatesConfig.DROP_RATES) * boostDropRate / 100f;
	}

	public float calculateEffectiveChance(GlobalRule rule, Npc npc, DropModifiers dropModifiers) {
		float chance = rule.getChance();
		// dynamic_chance means mobs will have different base chances based on their rank and rating
		if (rule.isDynamicChance())
			chance *= getRankModifier(npc) * getRatingModifier(npc);
		return dropModifiers.calculateDropChance(chance, rule.isUseLevelBasedChanceReduction());
	}

	private int addDropItems(int index, Set<DropItem> droppedItems, GlobalRule rule, Npc npc, Player player, Collection<Player> groupMembers,
		int winnerObj, DropModifiers dropModifiers) {
		List<GlobalDropItem> drops = collectDrops(rule, npc, dropModifiers);
		if (!drops.isEmpty()) {
			if (rule.getMemberLimit() > 1 && player.isInTeam()) {
				List<Player> members = new ArrayList<>(groupMembers);
				if (rule.getMemberLimit() > members.size())
					Collections.shuffle(members);
				int distributedItems = 0;
				for (Player member : members) {
					for (GlobalDropItem drop : drops) {
						DropItem dropitem = new DropItem(new Drop(drop.getId(), 1, 1, 100));
						dropitem.setCount(getItemCount(drop, npc));
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
				for (GlobalDropItem drop : drops) {
					droppedItems.add(regDropItem(index++, winnerObj, npc.getObjectId(), drop.getId(), getItemCount(drop, npc)));
				}
			}
		}
		return index;
	}

	public DropItem regDropItem(int index, int playerObjId, int objId, int itemId, long count) {
		DropItem item = new DropItem(new Drop(itemId, 1, 1, 100));
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

	public List<GlobalDropItem> collectDrops(GlobalRule rule, Npc npc, DropModifiers dropModifiers) {
		int maxDrops = dropModifiers.getMaxDropsPerGroup() == null ? rule.getMaxDropRule() : dropModifiers.getMaxDropsPerGroup();
		List<GlobalDropItem> drops = collectAllowedDrops(rule, npc, dropModifiers);
		if (drops.size() > maxDrops) {
			List<GlobalDropItem> allowedItems = new ArrayList<>();
			for (int i = 0; i < maxDrops && !drops.isEmpty(); i++) {
				GlobalDropItem item = Chance.selectElement(drops, true);
				if (item != null)
					allowedItems.add(item);
			}
			return allowedItems;
		}
		return drops;
	}

	private List<GlobalDropItem> collectAllowedDrops(GlobalRule rule, Npc npc, DropModifiers dropModifiers) {
		if (!checkRuleRestrictions(rule, dropModifiers.getDropRace(), npc))
			return Collections.emptyList();
		List<GlobalDropItem> tempItems = new ArrayList<>();
		for (GlobalDropItem globalItem : rule.getDropItems()) {
			ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(globalItem.getId());
			if (itemTemplate.getRace() == Race.PC_ALL || itemTemplate.getRace() == dropModifiers.getDropRace()) {
				int diff = npc.getLevel() - itemTemplate.getLevel();
				if (diff >= rule.getMinDiff() && diff <= rule.getMaxDiff())
					tempItems.add(globalItem);
			}
		}
		return tempItems;
	}

	@SuppressWarnings("lossy-conversions")
	private long getItemCount(GlobalDropItem item, Npc npc) {
		long count = Rnd.get(item.getMinCount(), item.getMaxCount());
		if (item.getId() == ItemId.KINAH)
			count *= npc.getLevel() * Math.pow(getRankModifier(npc) * getRatingModifier(npc), 6);
		return count;
	}

	private float getRankModifier(Npc npc) {
		return switch (npc.getRank()) {
			case NOVICE -> 0.9f;
			case DISCIPLINED -> 1f;
			case SEASONED -> 1.05f;
			case EXPERT -> 1.1f;
			case VETERAN -> 1.15f;
			case MASTER -> 1.2f;
		};
	}

	private float getRatingModifier(Npc npc) {
		return switch (npc.getRating()) {
			case JUNK -> 0.5f;
			case NORMAL -> 1f;
			case ELITE -> 1.3f;
			case HERO -> 1.8f;
			case LEGENDARY -> 2f;
		};
	}

	private static class SingletonHolder {

		protected static final DropRegistrationService instance = new DropRegistrationService();
	}

}
