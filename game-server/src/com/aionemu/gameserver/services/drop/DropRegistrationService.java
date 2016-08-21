package com.aionemu.gameserver.services.drop;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.drop.NpcDrop;
import com.aionemu.gameserver.model.gameobjects.DropNpc;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.team2.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropExcludedNpc;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropItem;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropMap;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropNpc;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropNpcGroup;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropRace;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropRating;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropTribe;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropWorld;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropZone;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalExclusion;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalRule;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_STATUS;
import com.aionemu.gameserver.services.EventService;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.DropRewardEnum;
import com.aionemu.gameserver.world.WorldDropType;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.zone.ZoneName;

import javolution.util.FastTable;

/**
 * @author xTz, Aioncool, Bobobear
 * @modified Neon
 */
public class DropRegistrationService {

	private Map<Integer, Set<DropItem>> currentDropMap = new ConcurrentHashMap<>();
	private Map<Integer, DropNpc> dropRegistrationMap = new ConcurrentHashMap<>();
	private List<Integer> noReductionMaps = new FastTable<>();

	private DropRegistrationService() {
		for (String zone : DropConfig.DISABLE_DROP_REDUCTION_IN_ZONES.split(","))
			noReductionMaps.add(Integer.parseInt(zone));
	}

	public void registerDrop(Npc npc, Player player, Collection<Player> groupMembers) {
		registerDrop(npc, player, player.getLevel(), groupMembers);
	}

	/**
	 * After NPC dies, it can register arbitrary drop
	 */
	public void registerDrop(Npc npc, Player player, int heighestLevel, Collection<Player> groupMembers) {
		if (player == null) {
			return;
		}
		int npcObjId = npc.getObjectId();

		// Getting all possible drops for this Npc
		NpcDrop npcDrop = DataManager.CUSTOM_NPC_DROP.getNpcDrop(npc.getNpcId());
		Set<DropItem> droppedItems = new HashSet<>();
		int index = 1;
		int dropChance = 100;
		int npcLevel = npc.getLevel();
		String dropType = npc.getGroupDrop().name().toLowerCase();
		boolean isChest = npc.getAi2().getName().equals("chest") || dropType.startsWith("treasure") || dropType.endsWith("box");
		if (!DropConfig.DISABLE_DROP_REDUCTION && ((isChest && npcLevel != 1 || !isChest)) && !noReductionMaps.contains(npc.getWorldId())) {
			dropChance = DropRewardEnum.dropRewardFrom(npcLevel - heighestLevel); // reduce chance depending on level
		}

		// Generete drop by this player
		Player genesis = player;
		Integer winnerObj = 0;

		// Distributing drops to players
		Collection<Player> dropPlayers = new FastTable<>();
		Collection<Player> winningPlayers = new FastTable<>();
		if (player.isInGroup2() || player.isInAlliance2()) {
			List<Integer> dropMembers = new FastTable<>();
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
							winningPlayers.add(p);
							winnerObj = p.getObjectId();
							setItemsToWinner(droppedItems, winnerObj);
							genesis = p;
							break;
						}
					}
					break;
				case FREEFORALL:
					winningPlayers = groupMembers;
					break;
				case LEADER:
					Player leader = player.isInGroup2() ? player.getPlayerGroup2().getLeaderObject() : player.getPlayerAlliance2().getLeaderObject();
					winningPlayers.add(leader);
					winnerObj = leader.getObjectId();
					setItemsToWinner(droppedItems, winnerObj);

					genesis = leader;
					break;
			}

			for (Player member : winningPlayers) {
				dropMembers.add(member.getObjectId());
				dropPlayers.add(member);
			}
			DropNpc dropNpc = new DropNpc(npcObjId);
			dropRegistrationMap.put(npcObjId, dropNpc);
			dropNpc.setPlayersObjectId(dropMembers);
			dropNpc.setInRangePlayers(groupMembers);
			dropNpc.setGroupSize(groupMembers.size());
		} else {
			List<Integer> singlePlayer = new FastTable<>();
			singlePlayer.add(player.getObjectId());
			dropPlayers.add(player);
			dropRegistrationMap.put(npcObjId, new DropNpc(npcObjId));
			dropRegistrationMap.get(npcObjId).setPlayersObjectId(singlePlayer);
		}

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
		boostDropRate += genesis.getActiveHouse() != null ? genesis.getActiveHouse().getHouseType().equals(HouseType.PALACE) ? 0.05f : 0 : 0;
		// Hmm.. 169625013 have boost drop rate 5% info but no such desc on buff

		// can be exploited on duel with Spiritmaster Erosion skill
		boostDropRate += genesis.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f - 1;

		float dropRate = genesis.getRates().getDropRate() * boostDropRate * dropChance / 100F;

		if (npcDrop != null) {
			index = npcDrop.dropCalculator(droppedItems, index, dropRate, genesis.getRace(), groupMembers);
		}

		// Updating current dropMap
		currentDropMap.put(npcObjId, droppedItems);

		index = QuestService.getQuestDrop(droppedItems, index, npc, groupMembers, genesis);

		if (EventsConfig.ENABLE_EVENT_SERVICE) {
			boolean isNpcQuest = npc.getAi2().getName().equals("quest_use_item");
			// if npc ai == quest_use_item it will be always excluded from event drops
			// also check if npc must be excluded due to global npc restrictions
			if (!isNpcQuest && !hasGlobalNpcExclusions(npc)) {
				for (EventTemplate eventTemplate : EventService.getInstance().getEnabledEvents()) {
					if (eventTemplate.getEventDrops() == null || !eventTemplate.isActive()) {
						continue;
					}
					// instances with world drop type == None must not have global drops (example Arenas)
					if (npc.getWorldDropType().equals(WorldDropType.NONE)) {
						continue;
					}

					List<GlobalRule> eventDropRules = eventTemplate.getEventDrops().getAllRules();

					for (GlobalRule rule : eventDropRules) {
						if (rule.getGlobalRuleItems() == null) {
							continue;
						}

						// if getGlobalRuleNpcs() != null means drops are for specified npcs (like named drops)
						// so the following restrictions will be ignored
						if (rule.getGlobalRuleNpcs() == null) {
							// EXCLUSIONS:
							// siege spawns, base spawns, rift spawns and vortex spawns must not have drops
							if (npc.getSpawn() instanceof SiegeSpawnTemplate && npc.getAbyssNpcType() != AbyssNpcType.DEFENDER)
								continue;
							// if (npc.getSpawn() instanceof RiftSpawnTemplate || npc.getSpawn() instanceof VortexSpawnTemplate)
							// continue;

							// exclude Inner Base Npcs
							if (npc.getSpawn() instanceof BaseSpawnTemplate) {
								if (npc.getSpawn().getHandlerType() != SpawnHandlerType.OUTRIDER
									&& npc.getSpawn().getHandlerType() != SpawnHandlerType.OUTRIDER_ENHANCED)
									continue;
							}
							// if npc level ==1 means missing stats, so better exclude it from drops
							if (npc.getLevel() < 2 && !isChest && npc.getWorldId() != WorldMapType.POETA.getId()
								&& npc.getWorldId() != WorldMapType.ISHALGEN.getId()) {
								continue;
							}
							// if abyss type npc != null or npc is chest, the npc will be excluded from drops
							if ((!isChest && npc.getAbyssNpcType() != AbyssNpcType.NONE) || isChest) {
								continue;
							}
						}

						float chance = rule.getChance();
						// if fixed_chance == true means all mobs will have the same base chance (npcRating and npcRank will be excluded from calculation)
						if (!rule.isFixedChance())
							chance *= getRankModifier(npc) * getRatingModifier(npc);
						// ignore dropRate if it's a noReduction rule (would be 0 since it includes the dropChance)
						if (!rule.getNoReduction())
							chance *= dropRate;
						if (Rnd.get() * 100 > chance)
							continue;

						if (!DropConfig.DISABLE_DROP_REDUCTION && ((isChest && npc.getLevel() != 1 || !isChest)) && !noReductionMaps.contains(npc.getWorldId())) {
							if ((player.getLevel() - npc.getLevel()) >= 10 && !rule.getNoReduction())
								continue;
						}
						if (!checkRestrictionRace(rule, player))
							continue;
						if (!checkGlobalRuleMaps(rule, npc))
							continue;
						if (!checkGlobalRuleWorlds(rule, npc))
							continue;
						if (!checkGlobalRuleRatings(rule, npc))
							continue;
						if (!checkGlobalRuleRaces(rule, npc))
							continue;
						if (!checkGlobalRuleTribes(rule, npc))
							continue;
						if (!checkGlobalRuleZones(rule, npc))
							continue;
						if (!checkGlobalRuleNpcs(rule, npc))
							continue;
						if (!checkGlobalRuleNpcGroups(rule, npc)) // drop group from npc_templates
							continue;
						// not used anymore, converted into Ids during Load Static Data
						// if (!checkGlobalRuleNpcNames (rule, npc))
						// continue;
						if (checkGlobalRuleExcludedNpcs(rule, npc))
							continue;
						List<Integer> alloweditems = getAllowedItems(rule, npc, player);
						if (alloweditems.size() == 0)
							continue;

						if (rule.getMemberLimit() > 1 && (player.isInGroup2() || player.isInAlliance2() || player.isInLeague())) {
							final int limit = rule.getMemberLimit();
							int distributedItems = 0;
							for (Player member : winningPlayers) {
								for (int itemListed : alloweditems) {
									DropItem dropitem = new DropItem(new Drop(itemListed, 1, 1, 100, false));
									dropitem.setCount(getItemCount(itemListed, rule, npc));
									dropitem.setIndex(index++);
									dropitem.setPlayerObjId(member.getObjectId());
									dropitem.setWinningPlayer(member);
									dropitem.isDistributeItem(true);
									droppedItems.add(dropitem);
								}
								if (++distributedItems >= limit)
									break;
							}
						} else {
							for (int itemListed : alloweditems) {
								droppedItems.add(regDropItem(index++, winnerObj, npcObjId, itemListed, getItemCount(itemListed, rule, npc)));
							}
						}
					}
				}
			}
		}

		boolean isNpcQuest = npc.getAi2().getName().equals("quest_use_item");
		// if npc ai == quest_use_item it will be always excluded from global drops
		if (!isNpcQuest && !hasGlobalNpcExclusions(npc)) {
			List<GlobalRule> globalrules = DataManager.GLOBAL_DROP_DATA.getAllRules();
			for (GlobalRule rule : globalrules) {
				if (rule.getGlobalRuleItems() == null) {
					continue;
				}
				// instances with world drop type == None must not have global drops (example Arenas)
				if (npc.getWorldDropType().equals(WorldDropType.NONE)) {
					continue;
				}
				// if getGlobalRuleNpcs() != null means drops are for specified npcs (like named drops)
				// so the following restrictions will be ignored
				if (rule.getGlobalRuleNpcs() == null) {
					// EXCLUSIONS:
					// siege spawns, base spawns, rift spawns and vortex spawns must not have drops
					if (npc.getSpawn() instanceof SiegeSpawnTemplate && npc.getAbyssNpcType() != AbyssNpcType.DEFENDER)
						continue;
					// if (npc.getSpawn() instanceof RiftSpawnTemplate || npc.getSpawn() instanceof VortexSpawnTemplate)
					// continue;

					// exclude Inner Base Npcs
					if (npc.getSpawn() instanceof BaseSpawnTemplate) {
						if (npc.getSpawn().getHandlerType() != SpawnHandlerType.OUTRIDER && npc.getSpawn().getHandlerType() != SpawnHandlerType.OUTRIDER_ENHANCED)
							continue;
					}

					// if npc level ==1 means missing stats, so better exclude it from drops
					if (npc.getLevel() < 2 && !isChest && npc.getWorldId() != WorldMapType.POETA.getId() && npc.getWorldId() != WorldMapType.ISHALGEN.getId()) {
						continue;
					}
					// if abyss type npc != null or npc is chest, the npc will be excluded from drops
					if ((!isChest && npc.getAbyssNpcType() != AbyssNpcType.NONE && npc.getAbyssNpcType() != AbyssNpcType.DEFENDER) || isChest) {
						continue;
					}
				}

				float chance = rule.getChance();
				// if fixed_chance == true means all mobs will have the same base chance (npcRating and npcRank will be excluded from calculation)
				if (!rule.isFixedChance())
					chance *= getRankModifier(npc) * getRatingModifier(npc);
				// ignore dropRate if it's a noReduction rule (would be 0 since it includes the dropChance)
				if (!rule.getNoReduction())
					chance *= dropRate;
				if (Rnd.get() * 100 > chance)
					continue;

				if (!DropConfig.DISABLE_DROP_REDUCTION && ((isChest && npc.getLevel() != 1) || !isChest) && !noReductionMaps.contains(npc.getWorldId())) {
					if ((player.getLevel() - npc.getLevel()) >= 10 && !rule.getNoReduction())
						continue;
				}
				if (!checkRestrictionRace(rule, player))
					continue;
				if (!checkGlobalRuleMaps(rule, npc))
					continue;
				if (!checkGlobalRuleWorlds(rule, npc))
					continue;
				if (!checkGlobalRuleRatings(rule, npc))
					continue;
				if (!checkGlobalRuleRaces(rule, npc))
					continue;
				if (!checkGlobalRuleTribes(rule, npc))
					continue;
				if (!checkGlobalRuleZones(rule, npc))
					continue;
				if (!checkGlobalRuleNpcs(rule, npc))
					continue;
				if (!checkGlobalRuleNpcGroups(rule, npc)) // drop group from npc_templates
					continue;
				// not used anymore, converted into Ids during Load Static Data
				// if (!checkGlobalRuleNpcNames (rule, npc))
				// continue;
				if (checkGlobalRuleExcludedNpcs(rule, npc))
					continue;
				List<Integer> alloweditems = getAllowedItems(rule, npc, player);
				if (alloweditems.size() == 0)
					continue;

				if (rule.getMemberLimit() > 1 && (player.isInGroup2() || player.isInAlliance2() || player.isInLeague())) {
					final int limit = rule.getMemberLimit();
					int distributedItems = 0;
					for (Player member : winningPlayers) {
						for (int itemListed : alloweditems) {
							DropItem dropitem = new DropItem(new Drop(itemListed, 1, 1, 100, false));
							dropitem.setCount(getItemCount(itemListed, rule, npc));
							dropitem.setIndex(index++);
							dropitem.setPlayerObjId(member.getObjectId());
							dropitem.setWinningPlayer(member);
							dropitem.isDistributeItem(true);
							droppedItems.add(dropitem);
						}
						if (++distributedItems >= limit)
							break;
					}
				} else {
					for (int itemListed : alloweditems) {
						droppedItems.add(regDropItem(index++, winnerObj, npcObjId, itemListed, getItemCount(itemListed, rule, npc)));
					}
				}
			}
		}

		if (npc.getPosition().isInstanceMap()) {
			npc.getPosition().getWorldMapInstance().getInstanceHandler().onDropRegistered(npc);
		}
		npc.getAi2().onGeneralEvent(AIEventType.DROP_REGISTERED);

		for (Player p : dropPlayers) {
			PacketSendUtility.sendPacket(p, new SM_LOOT_STATUS(npcObjId, 0));
		}

		DropService.getInstance().scheduleFreeForAll(npcObjId);
	}

	public void setItemsToWinner(Set<DropItem> droppedItems, Integer obj) {
		for (DropItem dropItem : droppedItems) {
			if (!dropItem.getDropTemplate().isEachMember()) {
				if (obj == 0)
					dropItem.getPlayerObjIds().clear();
				else
					dropItem.setPlayerObjId(obj);
			}
		}
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
		for (GlobalExclusion gde : DataManager.GLOBAL_EXCLUSION_DATA.getGlobalExclusions()) {
			if (gde.getNpcIds() != null && gde.getNpcIds().contains(npc.getNpcId())
				|| gde.getNpcNames() != null && gde.getNpcNames().contains(npc.getName())
				|| gde.getNpcTemplateTypes() != null && gde.getNpcTemplateTypes().contains(npc.getNpcTemplateType())
				|| gde.getNpcTribes() != null && npc.getTribe() != null && gde.getNpcTribes().contains(npc.getTribe())
				|| gde.getNpcAbyssTypes() != null && gde.getNpcAbyssTypes().contains(npc.getAbyssNpcType()))
				return true;
		}
		return false;
	}

	public boolean checkRestrictionRace(GlobalRule rule, Player player) {
		if (rule.getRestrictionRace() != null) {
			if (player.getRace() == Race.ASMODIANS && rule.getRestrictionRace().equals(GlobalRule.RestrictionRace.ELYOS)
				|| player.getRace() == Race.ELYOS && rule.getRestrictionRace().equals(GlobalRule.RestrictionRace.ASMODIANS))
				return false;
		}
		return true;
	}

	public boolean checkGlobalRuleMaps(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleMaps() != null) {
			boolean stepCheck = false;
			for (GlobalDropMap gdMap : rule.getGlobalRuleMaps().getGlobalDropMaps()) {
				if (gdMap.getMapId() == npc.getPosition().getMapId()) {
					stepCheck = true;
					break;
				}
			}
			return stepCheck;
		}
		return true;
	}

	public boolean checkGlobalRuleWorlds(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleWorlds() != null) {
			boolean stepCheck = false;
			for (GlobalDropWorld gdWorld : rule.getGlobalRuleWorlds().getGlobalDropWorlds()) {
				if (gdWorld.getWorldDropType().equals(npc.getWorldDropType())) {
					stepCheck = true;
					break;
				}
			}
			return stepCheck;
		}
		return true;
	}

	public boolean checkGlobalRuleRatings(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleRatings() != null) {
			boolean stepCheck = false;
			for (GlobalDropRating gdRating : rule.getGlobalRuleRatings().getGlobalDropRatings()) {
				if (gdRating.getRating().equals(npc.getRating())) {
					stepCheck = true;
					break;
				}
			}
			return stepCheck;
		}
		return true;
	}

	public boolean checkGlobalRuleRaces(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleRaces() != null) {
			boolean stepCheck = false;
			for (GlobalDropRace gdRace : rule.getGlobalRuleRaces().getGlobalDropRaces()) {
				if (gdRace.getRace().equals(npc.getRace())) {
					stepCheck = true;
					break;
				}
			}
			return stepCheck;
		}
		return true;
	}

	public boolean checkGlobalRuleTribes(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleTribes() != null) {
			boolean stepCheck = false;
			for (GlobalDropTribe gdTribe : rule.getGlobalRuleTribes().getGlobalDropTribes()) {
				if (gdTribe.getTribe().equals(npc.getTribe())) {
					stepCheck = true;
					break;
				}
			}
			return stepCheck;
		}
		return true;
	}

	public boolean checkGlobalRuleZones(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleZones() != null) {
			boolean stepCheck = false;
			for (GlobalDropZone gdZone : rule.getGlobalRuleZones().getGlobalDropZones()) {
				if (npc.isInsideZone(ZoneName.get(gdZone.getZone()))) {
					stepCheck = true;
					break;
				}
			}
			return stepCheck;
		}
		return true;
	}

	public boolean checkGlobalRuleNpcs(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleNpcs() != null) {
			boolean stepCheck = false;
			for (GlobalDropNpc gdNpc : rule.getGlobalRuleNpcs().getGlobalDropNpcs()) {
				if (gdNpc.getNpcId() == npc.getNpcId()) {
					stepCheck = true;
					break;
				}
			}
			return stepCheck;
		}
		return true;
	}

	public boolean checkGlobalRuleNpcGroups(GlobalRule rule, Npc npc) {
		if (rule.getGlobalRuleNpcGroups() != null) {
			boolean stepCheck = false;
			for (GlobalDropNpcGroup gdGroup : rule.getGlobalRuleNpcGroups().getGlobalDropNpcGroups()) {
				if (gdGroup.getGroup().equals(npc.getGroupDrop())) {
					stepCheck = true;
					break;
				}
			}
			return stepCheck;
		}
		return true;
	}

	// not used anymore, converted into npc Ids during Load Static Data
	/**
	 * private boolean checkGlobalRuleNpcNames (GlobalRule rule, Npc npc) { if (rule.getGlobalRuleNpcNames() != null) { boolean stepCheck= false; for
	 * (GlobalDropNpcName gdNpcName : rule.getGlobalRuleNpcNames().getGlobalDropNpcNames()) { if
	 * (gdNpcName.getFunction().equals(StringFunction.CONTAINS) && npc.getName().toLowerCase().contains(gdNpcName.getValue().toLowerCase())) stepCheck =
	 * true; if (gdNpcName.getFunction().equals(StringFunction.END_WITH) && npc.getName().toLowerCase().endsWith(gdNpcName.getValue().toLowerCase()))
	 * stepCheck = true; if (gdNpcName.getFunction().equals(StringFunction.START_WITH) &&
	 * npc.getName().toLowerCase().startsWith(gdNpcName.getValue().toLowerCase())) stepCheck = true; if
	 * (gdNpcName.getFunction().equals(StringFunction.EQUALS) && npc.getName().toLowerCase().equals(gdNpcName.getValue().toLowerCase())) stepCheck =
	 * true; } return stepCheck; } return true; }
	 **/
	public boolean checkGlobalRuleExcludedNpcs(GlobalRule rule, Npc npc) {
		boolean stepCheck = false;
		if (rule.getGlobalRuleExcludedNpcs() != null) {
			for (GlobalDropExcludedNpc gdExcludedNpc : rule.getGlobalRuleExcludedNpcs().getGlobalDropExcludedNpcs()) {
				if (gdExcludedNpc.getNpcId() == npc.getNpcId()) {
					stepCheck = true;
					break;
				}
			}
		}
		return stepCheck;
	}

	public List<Integer> getAllowedItems(GlobalRule rule, Npc npc, Player player) {
		List<Integer> alloweditems = new FastTable<>();
		List<Integer> droppeditems = new FastTable<>();
		for (GlobalDropItem globalItem : rule.getGlobalRuleItems().getGlobalDropItems()) {
			// check for prevent different race drops
			if (player.getRace() == Race.ASMODIANS && globalItem.getItemTemplate().getRace().equals(Race.ELYOS)
				|| player.getRace() == Race.ELYOS && globalItem.getItemTemplate().getRace().equals(Race.ASMODIANS)) {
				continue;
			}
			int diff = npc.getLevel() - globalItem.getItemTemplate().getLevel();
			if (diff >= rule.getMinDiff() && diff <= rule.getMaxDiff()) {
				alloweditems.add(globalItem.getId());
			}
		}
		if (alloweditems.size() >= 1) {
			for (int i = 0; i < rule.getMaxDropRule(); i++) {
				int rndIndex = Rnd.get(alloweditems.size());
				droppeditems.add(alloweditems.get(rndIndex));
				alloweditems.remove(rndIndex);
				if (alloweditems.size() == 0)
					break;
			}
		}
		return droppeditems;
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

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final DropRegistrationService instance = new DropRegistrationService();
	}

}
