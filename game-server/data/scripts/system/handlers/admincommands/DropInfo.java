package admincommands;

import java.util.ArrayList;
import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.GlobalDropData;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropGroup;
import com.aionemu.gameserver.model.drop.NpcDrop;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
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
import com.aionemu.gameserver.model.templates.npc.NpcRank;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.vortexspawns.VortexSpawnTemplate;
import com.aionemu.gameserver.services.EventService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.WorldDropType;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.zone.ZoneName;

// TODO simplify this by modifying the drop system classes
/**
 * @author Oliver, modified AionCool, Bobobear
 */
public class DropInfo extends AdminCommand {

	public DropInfo() {
		super("dropinfo");
	}

	@Override
	public void execute(Player player, String... params) {
		Npc currentNpc = null;
		NpcDrop npcDrop = null;
		if (params.length > 0) {
			int npcId = Integer.parseInt(params[0]);
			NpcTemplate npcTemplate = DataManager.NPC_DATA.getNpcTemplate(npcId);
			if (npcTemplate == null) {
				PacketSendUtility.sendMessage(player, "Incorrect npcId: " + npcId);
				return;
			}
			npcDrop = npcTemplate.getNpcDrop();
		} else {
			VisibleObject visibleObject = player.getTarget();

			if (visibleObject == null) {
				PacketSendUtility.sendMessage(player, "You should target some NPC first !");
				return;
			}

			if (visibleObject instanceof Npc) {
				npcDrop = ((Npc) visibleObject).getNpcDrop();
				currentNpc = ((Npc) visibleObject);
			}
		}
		if (npcDrop == null && !EventsConfig.ENABLE_EVENT_SERVICE && !DropConfig.ENABLE_GLOBAL_DROPS) {
			PacketSendUtility.sendMessage(player, "No drops for the selected NPC");
			return;
		}

		int count = 0;
		PacketSendUtility.sendMessage(player, "\n[Drop Info for the specified NPC]");
		if (npcDrop != null) {
			for (DropGroup dropGroup : npcDrop.getDropGroup()) {
				String maxItems = (DropConfig.DROP_ENABLE_SUPPORT_NEW_DROP_CATEGORY_CALCULATION && DropConfig.DROP_ENABLE_SUPPORT_NEW_NPCDROPS_FILES ? " MaxDropGroup: "
					+ dropGroup.getMaxItems()
					: (dropGroup.isUseCategory() ? " MaxDropGroup: 1" : " MaxDropGroup: 99"));
				PacketSendUtility.sendMessage(player, "DropGroup: " + dropGroup.getGroupName() + maxItems);
				for (Drop drop : dropGroup.getDrop()) {
					PacketSendUtility.sendMessage(player, "[item:" + drop.getItemId() + "]" + "	Rate: " + drop.getChance());
					count++;
				}
			}
		}
		if (EventsConfig.ENABLE_EVENT_SERVICE) {
			PacketSendUtility.sendMessage(player, "Events Enabled");
			// Drop rate from NPC can be boosted by Spiritmaster Erosion skill
			float boostDropRate = currentNpc.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f;

			// Drop rate can be boosted by player buff too
			boostDropRate += player.getGameStats().getStat(StatEnum.DR_BOOST, 0).getCurrent() / 100f;

			// Some personal drop boost
			// EoR 10% Boost drop rate
			boostDropRate += player.getCommonData().getCurrentReposteEnergy() > 0 ? 0.1f : 0;
			// EoS 5% Boost drop rate
			boostDropRate += player.getCommonData().getCurrentSalvationPercent() > 0 ? 0.05f : 0;
			// Deed to Palace 5% Boost drop rate
			boostDropRate += player.getActiveHouse() != null ? player.getActiveHouse().getHouseType().equals(HouseType.PALACE) ? 0.05f : 0 : 0;

			boostDropRate += player.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f - 1;

			FastTable<Integer> noReductionMaps = new FastTable<Integer>();
			for (String zone : DropConfig.DISABLE_DROP_REDUCTION_IN_ZONES.split(",")) {
				noReductionMaps.add(Integer.parseInt(zone));
			}

			boolean isNpcQuest = currentNpc.getAi2().getName().equals("quest_use_item");
			boolean isNpcChest = currentNpc.getAi2().getName().equals("chest");

			// some exclusion from drops
			if (!isNpcQuest && !hasGlobalNpcExclusions(currentNpc)) {
				List<EventTemplate> activeEvents = EventService.getInstance().getActiveEvents();
				for (EventTemplate eventTemplate : activeEvents) {
					if (eventTemplate.getEventDrops() == null) {
						continue;
					}
					List<GlobalRule> eventDropRules = eventTemplate.getEventDrops().getAllRules();

					for (GlobalRule rule : eventDropRules) {
						if (rule.getGlobalRuleItems() == null) {
							continue;
						}
						// instances with world drop type == None must not have global drops (example Arenas)
						if (currentNpc.getWorldDropType().equals(WorldDropType.NONE)) {
							continue;
						}
						// if getGlobalRuleNpcs() != null means drops are for specified npcs (like named drops)
						// so the following restrictions will be ignored
						if (rule.getGlobalRuleNpcs() == null) {
							// EXCLUSIONS:
							// siege spawns, base spawns, rift spawns and vortex spawns must not have drops
							if (currentNpc.getSpawn() instanceof SiegeSpawnTemplate || currentNpc.getSpawn() instanceof RiftSpawnTemplate
								|| currentNpc.getSpawn() instanceof VortexSpawnTemplate || currentNpc.getSpawn() instanceof BaseSpawnTemplate) {
								continue;
							}
							// if npc level ==1 means missing stats, so better exclude it from drops
							if (currentNpc.getLevel() < 2 && !isNpcChest && currentNpc.getWorldId() != WorldMapType.POETA.getId()
								&& currentNpc.getWorldId() != WorldMapType.ISHALGEN.getId()) {
								continue;
							}
							// if abyss type npc != null or npc is chest, the npc will be excluded from drops
							if ((!isNpcChest && currentNpc.getAbyssNpcType() != AbyssNpcType.NONE) || isNpcChest) {
								continue;
							}
						}

						float chance = rule.getChance();
						// if fixed_chance == true means all mob will have the same base chance (npcRating and npcRank will be excluded from calculation)
						if (!rule.isFixedChance())
							chance *= getRankModifier(currentNpc) * getRatingModifier(currentNpc);

						float dropRate = player.getRates().getDropRate() * boostDropRate;
						@SuppressWarnings("unused")
						float percent = chance * dropRate > 100f ? 100 : chance * dropRate;

						if (!DropConfig.DISABLE_DROP_REDUCTION && ((isNpcChest && currentNpc.getLevel() != 1 || !isNpcChest))
							&& !noReductionMaps.contains(currentNpc.getWorldId())) {
							if ((player.getLevel() - currentNpc.getLevel()) >= 10 && !rule.getNoReduction())
								continue;
						}
						if (!checkRestrictionRace(rule, player))
							continue;
						if (!checkGlobalRuleMaps(rule, currentNpc))
							continue;
						if (!checkGlobalRuleWorlds(rule, currentNpc))
							continue;
						if (!checkGlobalRuleRatings(rule, currentNpc))
							continue;
						if (!checkGlobalRuleRaces(rule, currentNpc))
							continue;
						if (!checkGlobalRuleTribes(rule, currentNpc))
							continue;
						if (!checkGlobalRuleZones(rule, currentNpc))
							continue;
						if (!checkGlobalRuleNpcs(rule, currentNpc))
							continue;
						// not used anymore, converted into Ids during Load Static Data
						// if (!checkGlobalRuleNpcNames (rule, currentNpc))
						// continue;
						if (checkGlobalRuleExcludedNpcs(rule, currentNpc))
							continue;
						List<Integer> alloweditems = getAllowedItems(rule, currentNpc);
						if (alloweditems.size() == 0)
							continue;

						PacketSendUtility.sendMessage(player, "EventDropGroup: " + rule.getRuleName() + " MaxDropGroup: " + rule.getMaxDropRule());

						for (Integer itemId : alloweditems) {
							PacketSendUtility.sendMessage(player, "[item:" + itemId + "]" + "	Chance: " + chance);
							count++;
						}
					}
				}
			}
		}
		if (DropConfig.ENABLE_GLOBAL_DROPS) {
			PacketSendUtility.sendMessage(player, "GlobalDrops Enabled");
			// Drop rate from NPC can be boosted by Spiritmaster Erosion skill
			float boostDropRate = currentNpc.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f;

			// Drop rate can be boosted by player buff too
			boostDropRate += player.getGameStats().getStat(StatEnum.DR_BOOST, 0).getCurrent() / 100f;

			// Some personal drop boost
			// EoR 10% Boost drop rate
			boostDropRate += player.getCommonData().getCurrentReposteEnergy() > 0 ? 0.1f : 0;
			// EoS 5% Boost drop rate
			boostDropRate += player.getCommonData().getCurrentSalvationPercent() > 0 ? 0.05f : 0;
			// Deed to Palace 5% Boost drop rate
			boostDropRate += player.getActiveHouse() != null ? player.getActiveHouse().getHouseType().equals(HouseType.PALACE) ? 0.05f : 0 : 0;

			boostDropRate += player.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f - 1;

			FastTable<Integer> noReductionMaps = new FastTable<Integer>();
			for (String zone : DropConfig.DISABLE_DROP_REDUCTION_IN_ZONES.split(",")) {
				noReductionMaps.add(Integer.parseInt(zone));
			}

			boolean isNpcChest = currentNpc.getAi2().getName().equals("chest");
			boolean isNpcQuest = currentNpc.getAi2().getName().equals("quest_use_item");

			// if npc ai == quest_use_item it will be always excluded from global drops
			if (!isNpcQuest && !hasGlobalNpcExclusions(currentNpc)) {
				GlobalDropData globalDrops = DataManager.GLOBAL_DROP_DATA;
				List<GlobalRule> globalrules = globalDrops.getAllRules();
				for (GlobalRule rule : globalrules) {
					if (rule.getGlobalRuleItems() == null) {
						continue;
					}
					// instances with world drop type == None must not have global drops (example Arenas)
					if (currentNpc.getWorldDropType().equals(WorldDropType.NONE)) {
						continue;
					}

					// if getGlobalRuleNpcs() != null means drops are for specified npcs (like named drops)
					// so the following restrictions will be ignored
					if (rule.getGlobalRuleNpcs() == null) {
						// EXCLUSIONS:
						// siege spawns, base spawns, rift spawns and vortex spawns must not have drops
						if (currentNpc.getSpawn() instanceof SiegeSpawnTemplate || currentNpc.getSpawn() instanceof RiftSpawnTemplate
							|| currentNpc.getSpawn() instanceof VortexSpawnTemplate || currentNpc.getSpawn() instanceof BaseSpawnTemplate) {
							continue;
						}
						// if npc level ==1 means missing stats, so better exclude it from drops
						if (currentNpc.getLevel() < 2 && !isNpcChest && currentNpc.getWorldId() != WorldMapType.POETA.getId()
							&& currentNpc.getWorldId() != WorldMapType.ISHALGEN.getId()) {
							continue;
						}
						// if abyss type npc != null or npc is chest, the npc will be excluded from drops
						if ((!isNpcChest && currentNpc.getAbyssNpcType() != AbyssNpcType.NONE) || isNpcChest) {
							continue;
						}
					}

					float chance = rule.getChance();
					// if fixed_chance == true means all mob will have the same base chance (npcRating and npcRank will be excluded from calculation)
					if (!rule.isFixedChance())
						chance *= getRankModifier(currentNpc) * getRatingModifier(currentNpc);
					float dropRate = player.getRates().getDropRate() * boostDropRate;
					@SuppressWarnings("unused")
					float percent = chance * dropRate > 100f ? 100 : chance * dropRate;

					if (!DropConfig.DISABLE_DROP_REDUCTION && ((isNpcChest && currentNpc.getLevel() != 1 || !isNpcChest))
						&& !noReductionMaps.contains(currentNpc.getWorldId())) {
						if ((player.getLevel() - currentNpc.getLevel()) >= 10 && !rule.getNoReduction())
							continue;
					}
					if (!checkRestrictionRace(rule, player))
						continue;
					if (!checkGlobalRuleMaps(rule, currentNpc))
						continue;
					if (!checkGlobalRuleWorlds(rule, currentNpc))
						continue;
					if (!checkGlobalRuleRatings(rule, currentNpc))
						continue;
					if (!checkGlobalRuleRaces(rule, currentNpc))
						continue;
					if (!checkGlobalRuleTribes(rule, currentNpc))
						continue;
					if (!checkGlobalRuleZones(rule, currentNpc))
						continue;
					if (!checkGlobalRuleNpcs(rule, currentNpc))
						continue;
					if (!checkGlobalRuleNpcGroups(rule, currentNpc)) // drop group from npc_templates
						continue;
					// not used anymore, converted into Ids during Load Static Data
					// if (!checkGlobalRuleNpcNames (rule, currentNpc))
					// continue;
					if (checkGlobalRuleExcludedNpcs(rule, currentNpc))
						continue;
					List<Integer> alloweditems = getAllowedItems(rule, currentNpc);
					if (alloweditems.size() == 0)
						continue;

					PacketSendUtility.sendMessage(player, "GlobalDropGroup: " + rule.getRuleName() + " MaxDropGroup: " + rule.getMaxDropRule());

					for (Integer itemId : alloweditems) {
						PacketSendUtility.sendMessage(player, "[item:" + itemId + "]" + "	Chance: " + chance);
						count++;
					}
				}
			}
		}
		PacketSendUtility.sendMessage(player, count + " drops available for the selected NPC");
	}

	private boolean hasGlobalNpcExclusions(Npc npc) {
		for (GlobalExclusion gde : DataManager.GLOBAL_EXCLUSION_DATA.getGlobalExclusions()) {
			if (gde.getNpcIds() != null && gde.getNpcIds().contains(npc.getNpcId()) || gde.getNpcNames() != null
				&& gde.getNpcNames().contains(npc.getName()) || gde.getNpcTemplateTypes() != null
				&& gde.getNpcTemplateTypes().contains(npc.getNpcTemplateType()) || gde.getNpcTribes() != null && npc.getTribe() != null
				&& gde.getNpcTribes().contains(npc.getTribe()) || gde.getNpcAbyssTypes() != null && gde.getNpcAbyssTypes().contains(npc.getAbyssNpcType()))
				return true;
		}
		return false;
	}

	private boolean checkRestrictionRace(GlobalRule rule, Player player) {
		if (rule.getRestrictionRace() != null) {
			if (player.getRace() == Race.ASMODIANS && rule.getRestrictionRace().equals("ELYOS") || player.getRace() == Race.ELYOS
				&& rule.getRestrictionRace().equals("ASMODIANS"))
				return false;
		}
		return true;
	}

	private boolean checkGlobalRuleMaps(GlobalRule rule, Npc npc) {
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

	private boolean checkGlobalRuleWorlds(GlobalRule rule, Npc npc) {
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

	private boolean checkGlobalRuleRatings(GlobalRule rule, Npc npc) {
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

	private boolean checkGlobalRuleRaces(GlobalRule rule, Npc npc) {
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

	private boolean checkGlobalRuleTribes(GlobalRule rule, Npc npc) {
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

	private boolean checkGlobalRuleZones(GlobalRule rule, Npc npc) {
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

	private boolean checkGlobalRuleNpcs(GlobalRule rule, Npc npc) {
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

	private boolean checkGlobalRuleNpcGroups(GlobalRule rule, Npc npc) {
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

	// not used anymore, converted into Ids during Load Static Data
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
	private boolean checkGlobalRuleExcludedNpcs(GlobalRule rule, Npc npc) {
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

	private List<Integer> getAllowedItems(GlobalRule rule, Npc npc) {
		List<Integer> alloweditems = new ArrayList<Integer>();
		for (GlobalDropItem globalItem : rule.getGlobalRuleItems().getGlobalDropItems()) {
			int diff = npc.getLevel() - globalItem.getItemTemplate().getLevel();
			if (diff >= rule.getMinDiff() && diff <= rule.getMaxDiff()) {
				alloweditems.add(globalItem.getId());
			}
		}
		return alloweditems;
	}

	private float getRankModifier(Npc npc) {
		// Rank modifier : NOVICE:0.5f, DISCIPLINED:1f, SEASONED:1.5f, EXPERT:2f, VETERAN:2.5f, MASTER:3f;
		float rankModifier = 1f;
		if (npc.getRank() != null) {
			if (npc.getRank().equals(NpcRank.NOVICE))
				rankModifier = 0.5f;
			else if (npc.getRank().equals(NpcRank.DISCIPLINED))
				rankModifier = 1f;
			else if (npc.getRank().equals(NpcRank.SEASONED))
				rankModifier = 1.5f;
			else if (npc.getRank().equals(NpcRank.EXPERT))
				rankModifier = 2f;
			else if (npc.getRank().equals(NpcRank.VETERAN))
				rankModifier = 2.5f;
			else if (npc.getRank().equals(NpcRank.MASTER))
				rankModifier = 3f;
		}
		return rankModifier;
	}

	private float getRatingModifier(Npc npc) {
		// Rating modifier: JUNK: 0.5f, NORMAL:1, ELITE:1.5f, HERO:2f, LEGENDARY:2.2f;
		float ratingModifier = 1f;
		if (npc.getRating() != null) {
			if (npc.getRating().equals(NpcRating.JUNK))
				ratingModifier = 0.5f;
			else if (npc.getRating().equals(NpcRating.NORMAL))
				ratingModifier = 1f;
			else if (npc.getRating().equals(NpcRating.ELITE))
				ratingModifier = 1.5f;
			else if (npc.getRating().equals(NpcRating.HERO))
				ratingModifier = 2f;
			else if (npc.getRating().equals(NpcRating.LEGENDARY))
				ratingModifier = 2.2f;
		}
		return ratingModifier;
	}

	@Override
	public void info(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
