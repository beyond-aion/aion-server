package admincommands;

import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.GlobalDropData;
import com.aionemu.gameserver.model.drop.Drop;
import com.aionemu.gameserver.model.drop.DropGroup;
import com.aionemu.gameserver.model.drop.NpcDrop;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalDropItem;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalRule;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.model.templates.npc.AbyssNpcType;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.riftspawns.RiftSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.vortexspawns.VortexSpawnTemplate;
import com.aionemu.gameserver.services.EventService;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.stats.DropRewardEnum;
import com.aionemu.gameserver.world.WorldDropType;
import com.aionemu.gameserver.world.WorldMapType;

// TODO delete this crap, rework that fucking so called "drop system" and create a command that doesn't utilize a shit-ton of code duplication
/**
 * Don't rely on this command. Output is NOT guaranteed to be correct at all -_-
 * 
 * @author Oliver
 * @modified AionCool, Bobobear, Neon
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

		DropRegistrationService drs = DropRegistrationService.getInstance();
		int dropChance = 100;
		int npcLevel = currentNpc.getLevel();
		String dropType = currentNpc.getGroupDrop().name().toLowerCase();
		boolean isNpcChest = currentNpc.getAi2().getName().equals("chest") || dropType.startsWith("treasure") || dropType.endsWith("box");
		FastTable<Integer> noReductionMaps = new FastTable<Integer>();
		for (String zone : DropConfig.DISABLE_DROP_REDUCTION_IN_ZONES.split(",")) {
			noReductionMaps.add(Integer.parseInt(zone));
		}
		if (!DropConfig.DISABLE_DROP_REDUCTION && ((isNpcChest && npcLevel != 1 || !isNpcChest)) && !noReductionMaps.contains(currentNpc.getWorldId())) {
			dropChance = DropRewardEnum.dropRewardFrom(npcLevel - player.getLevel()); // reduce chance depending on level
		}

		// Drop rate from NPC can be boosted by Spiritmaster Erosion skill
		float boostDropRate = currentNpc.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f;

		// Drop rate can be boosted by player buff too
		boostDropRate += player.getGameStats().getStat(StatEnum.DR_BOOST, 0).getCurrent() / 100f;

		// Some personal drop boost
		// EoR 5% Boost drop rate
		boostDropRate += player.getCommonData().getCurrentReposeEnergy() > 0 ? 0.05f : 0;
		// EoS 5% Boost drop rate
		boostDropRate += player.getCommonData().getCurrentSalvationPercent() > 0 ? 0.05f : 0;
		// Deed to Palace 5% Boost drop rate
		boostDropRate += player.getActiveHouse() != null ? player.getActiveHouse().getHouseType().equals(HouseType.PALACE) ? 0.05f : 0 : 0;

		boostDropRate += player.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f - 1;

		float dropRate = player.getRates().getDropRate() * boostDropRate * dropChance / 100F;
		
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
			boolean isNpcQuest = currentNpc.getAi2().getName().equals("quest_use_item");

			// some exclusion from drops
			if (!isNpcQuest && !drs.hasGlobalNpcExclusions(currentNpc)) {
				for (EventTemplate eventTemplate : EventService.getInstance().getEnabledEvents()) {
					if (eventTemplate.getEventDrops() == null || !eventTemplate.isActive()) {
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
							chance *= drs.getRankModifier(currentNpc) * drs.getRatingModifier(currentNpc);
						// ignore dropRate if it's a noReduction rule (would be 0 since it includes the dropChance) 
						if (!rule.getNoReduction())
							chance *= dropRate;

						if (!DropConfig.DISABLE_DROP_REDUCTION && ((isNpcChest && currentNpc.getLevel() != 1 || !isNpcChest))
							&& !noReductionMaps.contains(currentNpc.getWorldId())) {
							if ((player.getLevel() - currentNpc.getLevel()) >= 10 && !rule.getNoReduction())
								continue;
						}
						if (!drs.checkRestrictionRace(rule, player))
							continue;
						if (!drs.checkGlobalRuleMaps(rule, currentNpc))
							continue;
						if (!drs.checkGlobalRuleWorlds(rule, currentNpc))
							continue;
						if (!drs.checkGlobalRuleRatings(rule, currentNpc))
							continue;
						if (!drs.checkGlobalRuleRaces(rule, currentNpc))
							continue;
						if (!drs.checkGlobalRuleTribes(rule, currentNpc))
							continue;
						if (!drs.checkGlobalRuleZones(rule, currentNpc))
							continue;
						if (!drs.checkGlobalRuleNpcs(rule, currentNpc))
							continue;
						// not used anymore, converted into Ids during Load Static Data
						// if (!checkGlobalRuleNpcNames (rule, currentNpc))
						// continue;
						if (drs.checkGlobalRuleExcludedNpcs(rule, currentNpc))
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
			boolean isNpcQuest = currentNpc.getAi2().getName().equals("quest_use_item");

			// if npc ai == quest_use_item it will be always excluded from global drops
			if (!isNpcQuest && !drs.hasGlobalNpcExclusions(currentNpc)) {
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
						if (currentNpc.getSpawn() instanceof SiegeSpawnTemplate && currentNpc.getAbyssNpcType() != AbyssNpcType.DEFENDER || currentNpc.getSpawn() instanceof RiftSpawnTemplate
							|| currentNpc.getSpawn() instanceof VortexSpawnTemplate || currentNpc.getSpawn() instanceof BaseSpawnTemplate) {
							continue;
						}
						// if npc level ==1 means missing stats, so better exclude it from drops
						if (currentNpc.getLevel() < 2 && !isNpcChest && currentNpc.getWorldId() != WorldMapType.POETA.getId()
							&& currentNpc.getWorldId() != WorldMapType.ISHALGEN.getId()) {
							continue;
						}
						// if abyss type npc != null or npc is chest, the npc will be excluded from drops
						if ((!isNpcChest && currentNpc.getAbyssNpcType() != AbyssNpcType.NONE && currentNpc.getAbyssNpcType() != AbyssNpcType.DEFENDER) || isNpcChest) {
							continue;
						}
					}

					float chance = rule.getChance();
					// if fixed_chance == true means all mob will have the same base chance (npcRating and npcRank will be excluded from calculation)
					if (!rule.isFixedChance())
						chance *= drs.getRankModifier(currentNpc) * drs.getRatingModifier(currentNpc);
					// ignore dropRate if it's a noReduction rule (would be 0 since it includes the dropChance) 
					if (!rule.getNoReduction())
						chance *= dropRate;

					if (!DropConfig.DISABLE_DROP_REDUCTION && ((isNpcChest && currentNpc.getLevel() != 1 || !isNpcChest))
						&& !noReductionMaps.contains(currentNpc.getWorldId())) {
						if ((player.getLevel() - currentNpc.getLevel()) >= 10 && !rule.getNoReduction())
							continue;
					}
					if (!drs.checkRestrictionRace(rule, player))
						continue;
					if (!drs.checkGlobalRuleMaps(rule, currentNpc))
						continue;
					if (!drs.checkGlobalRuleWorlds(rule, currentNpc))
						continue;
					if (!drs.checkGlobalRuleRatings(rule, currentNpc))
						continue;
					if (!drs.checkGlobalRuleRaces(rule, currentNpc))
						continue;
					if (!drs.checkGlobalRuleTribes(rule, currentNpc))
						continue;
					if (!drs.checkGlobalRuleZones(rule, currentNpc))
						continue;
					if (!drs.checkGlobalRuleNpcs(rule, currentNpc))
						continue;
					if (!drs.checkGlobalRuleNpcGroups(rule, currentNpc)) // drop group from npc_templates
						continue;
					// not used anymore, converted into Ids during Load Static Data
					// if (!checkGlobalRuleNpcNames (rule, currentNpc))
					// continue;
					if (drs.checkGlobalRuleExcludedNpcs(rule, currentNpc))
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

	private List<Integer> getAllowedItems(GlobalRule rule, Npc npc) {
		List<Integer> alloweditems = new FastTable<Integer>();
		for (GlobalDropItem globalItem : rule.getGlobalRuleItems().getGlobalDropItems()) {
			int diff = npc.getLevel() - globalItem.getItemTemplate().getLevel();
			if (diff >= rule.getMinDiff() && diff <= rule.getMaxDiff()) {
				alloweditems.add(globalItem.getId());
			}
		}
		return alloweditems;
	}
}
