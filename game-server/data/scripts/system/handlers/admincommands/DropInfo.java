package admincommands;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
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
import com.aionemu.gameserver.model.templates.spawns.basespawns.BaseSpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.services.EventService;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
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
		VisibleObject visibleObject = player.getTarget();

		if (!(visibleObject instanceof Npc)) {
			sendInfo(player, "You should target some NPC first !");
			return;
		}

		Npc currentNpc = (Npc) visibleObject;
		NpcDrop npcDrop = DataManager.CUSTOM_NPC_DROP.getNpcDrop(currentNpc.getNpcId());

		DropRegistrationService drs = DropRegistrationService.getInstance();
		int dropChance = 100;
		int npcLevel = currentNpc.getLevel();
		String dropType = currentNpc.getGroupDrop().name().toLowerCase();
		boolean isNpcChest = currentNpc.getAi().getName().equals("chest") || dropType.startsWith("treasure") || dropType.endsWith("box");
		if (!DropConfig.DISABLE_REDUCTION && ((isNpcChest && npcLevel != 1 || !isNpcChest))
			&& !DropConfig.NO_REDUCTION_MAPS.contains(currentNpc.getWorldId())) {
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
		boostDropRate += player.getActiveHouse() != null && player.getActiveHouse().getHouseType() == HouseType.PALACE ? 0.05f : 0;

		boostDropRate += player.getGameStats().getStat(StatEnum.BOOST_DROP_RATE, 100).getCurrent() / 100f - 1;

		float dropRate = player.getRates().getDropRate() * boostDropRate * dropChance / 100F;

		int count = 0;
		sendInfo(player, "\n[Drop info for " + currentNpc.getName() + "]");
		if (npcDrop != null) {
			for (DropGroup dropGroup : npcDrop.getDropGroup()) {
				sendInfo(player, "DropGroup: " + dropGroup.getGroupName() + " MaxDropGroup: " + dropGroup.getMaxItems());
				for (Drop drop : dropGroup.getDrop()) {
					sendInfo(player, "[item:" + drop.getItemId() + "]" + "	Rate: " + drop.getChance());
					count++;
				}
			}
		}
		if (EventsConfig.ENABLE_EVENT_SERVICE) {
			sendInfo(player, "Events Enabled");
			boolean isNpcQuest = currentNpc.getAi().getName().equals("quest_use_item");

			// some exclusion from drops
			if (!isNpcQuest && !drs.hasGlobalNpcExclusions(currentNpc)) {
				for (EventTemplate eventTemplate : EventService.getInstance().getEnabledEvents()) {
					if (eventTemplate.getEventDrops() == null || !eventTemplate.isActive())
						continue;

					for (GlobalRule rule : eventTemplate.getEventDrops().getAllRules()) {
						if (rule.getGlobalRuleItems() == null)
							continue;

						// if getGlobalRuleNpcs() != null means drops are for specified npcs (like named drops)
						// so the following restrictions will be ignored
						if (rule.getGlobalRuleNpcs() == null) {
							// exclude siege spawns, and inner base spawns
							if (currentNpc.getSpawn() instanceof SiegeSpawnTemplate && currentNpc.getAbyssNpcType() != AbyssNpcType.DEFENDER)
								continue;
							if (currentNpc.getSpawn() instanceof BaseSpawnTemplate) {
								if (currentNpc.getSpawn().getHandlerType() != SpawnHandlerType.OUTRIDER
									&& currentNpc.getSpawn().getHandlerType() != SpawnHandlerType.OUTRIDER_ENHANCED)
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

						if (!DropConfig.DISABLE_REDUCTION && ((isNpcChest && currentNpc.getLevel() != 1 || !isNpcChest))
							&& !DropConfig.NO_REDUCTION_MAPS.contains(currentNpc.getWorldId())) {
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

						sendInfo(player, "EventDropGroup: " + rule.getRuleName() + " MaxDropGroup: " + rule.getMaxDropRule());

						for (Integer itemId : alloweditems) {
							sendInfo(player, "[item:" + itemId + "]" + "	Chance: " + chance);
							count++;
						}
					}
				}
			}
		}

		// if npc ai == quest_use_item it will be always excluded from global drops
		boolean isNpcQuest = currentNpc.getAi().getName().equals("quest_use_item");
		// instances with WorldDropType.NONE must not have global drops (example Arenas)
		if (!isNpcQuest && !drs.hasGlobalNpcExclusions(currentNpc) && currentNpc.getWorldDropType() != WorldDropType.NONE) {
			for (GlobalRule rule : DataManager.GLOBAL_DROP_DATA.getAllRules()) {
				if (rule.getGlobalRuleItems() == null)
					continue;

				// if getGlobalRuleNpcs() != null means drops are for specified npcs (like named drops)
				// so the following restrictions will be ignored
				if (rule.getGlobalRuleNpcs() == null) {
					// exclude most siege spawns, and inner base spawns
					if (currentNpc.getSpawn() instanceof SiegeSpawnTemplate && currentNpc.getAbyssNpcType() != AbyssNpcType.DEFENDER)
						continue;
					if (currentNpc.getSpawn() instanceof BaseSpawnTemplate) {
						if (currentNpc.getSpawn().getHandlerType() != SpawnHandlerType.OUTRIDER
							&& currentNpc.getSpawn().getHandlerType() != SpawnHandlerType.OUTRIDER_ENHANCED)
							continue;
					}
					// if npc level ==1 means missing stats, so better exclude it from drops
					if (currentNpc.getLevel() < 2 && !isNpcChest && currentNpc.getWorldId() != WorldMapType.POETA.getId()
						&& currentNpc.getWorldId() != WorldMapType.ISHALGEN.getId()) {
						continue;
					}
					// if abyss type npc != null or npc is chest, the npc will be excluded from drops
					if ((!isNpcChest && currentNpc.getAbyssNpcType() != AbyssNpcType.NONE && currentNpc.getAbyssNpcType() != AbyssNpcType.DEFENDER)
						|| isNpcChest) {
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

				if (!DropConfig.DISABLE_REDUCTION && ((isNpcChest && currentNpc.getLevel() != 1 || !isNpcChest))
					&& !DropConfig.NO_REDUCTION_MAPS.contains(currentNpc.getWorldId())) {
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

				sendInfo(player, "GlobalDropGroup: " + rule.getRuleName() + " MaxDropGroup: " + rule.getMaxDropRule());

				for (Integer itemId : alloweditems) {
					sendInfo(player, "[item:" + itemId + "]" + "	Chance: " + chance);
					count++;
				}
			}
		}
		sendInfo(player, count + " drops available for the selected NPC");
	}

	private List<Integer> getAllowedItems(GlobalRule rule, Npc npc) {
		List<Integer> alloweditems = new ArrayList<>();
		for (GlobalDropItem globalItem : rule.getGlobalRuleItems().getGlobalDropItems()) {
			int diff = npc.getLevel() - globalItem.getItemTemplate().getLevel();
			if (diff >= rule.getMinDiff() && diff <= rule.getMaxDiff()) {
				alloweditems.add(globalItem.getId());
			}
		}
		return alloweditems;
	}
}
