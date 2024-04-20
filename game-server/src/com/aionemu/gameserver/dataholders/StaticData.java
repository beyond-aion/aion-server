package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.dataholders.loadingutils.StaticDataListener;
import com.aionemu.gameserver.model.templates.mail.Mails;

/**
 * An instance of this class is the result of data loading.
 *
 * @author Luno, orz, Wakizashi
 */
@XmlRootElement(name = "static_data")
@XmlAccessorType(XmlAccessType.NONE)
public class StaticData {

	@XmlElement(name = "world_maps")
	public WorldMapsData worldMapsData;

	@XmlElement(name = "weather")
	public MapWeatherData mapWeatherData;

	@XmlElement(name = "npc_trade_list")
	public TradeListData tradeListData;

	@XmlElement(name = "npc_teleporter")
	public TeleporterData teleporterData;

	@XmlElement(name = "teleport_location")
	public TeleLocationData teleLocationData;

	@XmlElement(name = "bind_points")
	public BindPointData bindPointData;

	@XmlElement(name = "quests")
	public QuestsData questData;

	@XmlElement(name = "quest_scripts")
	public XMLQuests questsScriptData;

	@XmlElement(name = "player_experience_table")
	public PlayerExperienceTable playerExperienceTable;

	@XmlElement(name = "absolute_stats")
	public AbsoluteStatsData absoluteStatsData;

	@XmlElement(name = "item_templates")
	public ItemData itemData;

	@XmlElement(name = "random_bonuses")
	public ItemRandomBonusData itemRandomBonuses;

	@XmlElement(name = "npc_templates")
	public NpcData npcData;

	@XmlElement(name = "custom_drop")
	public CustomDrop customNpcDrop;

	@XmlElement(name = "npc_shouts")
	public NpcShoutData npcShoutData;

	@XmlElement(name = "player_initial_data")
	public PlayerInitialData playerInitialData;

	@XmlElement(name = "skill_data")
	public SkillData skillData;

	@XmlElement(name = "motion_times")
	public MotionData motionData;

	@XmlElement(name = "skill_tree")
	public SkillTreeData skillTreeData;

	@XmlElement(name = "cube_expander")
	public CubeExpandData cubeExpandData;

	@XmlElement(name = "warehouse_expander")
	public WarehouseExpandData warehouseExpandData;

	@XmlElement(name = "player_titles")
	public TitleData titleData;

	@XmlElement(name = "gatherable_templates")
	public GatherableData gatherableData;

	@XmlElement(name = "npc_walker")
	public WalkerData walkerData;

	@XmlElement(name = "zones")
	public ZoneData zoneData;

	@XmlElement(name = "goodslists")
	public GoodsListData goodsListData;

	@XmlElement(name = "tribe_relations")
	public TribeRelationsData tribeRelationsData;

	@XmlElement(name = "recipe_templates")
	public RecipeData recipeData;

	@XmlElement(name = "chest_templates")
	public ChestData chestData;

	@XmlElement(name = "staticdoor_templates")
	public StaticDoorData staticDoorData;

	@XmlElement(name = "item_sets")
	public ItemSetData itemSetData;

	@XmlElement(name = "npc_factions")
	public NpcFactionsData npcFactionsData;

	@XmlElement(name = "npc_skill_templates")
	public NpcSkillData npcSkillData;

	@XmlElement(name = "pet_skill_templates")
	public PetSkillData petSkillData;

	@XmlElement(name = "siege_locations")
	public SiegeLocationData siegeLocationData;

	@XmlElement(name = "dimensional_vortex")
	public VortexData vortexData;

	@XmlElement(name = "rift_locations")
	public RiftData riftData;

	@XmlElement(name = "base_locations")
	public BaseData baseData;

	@XmlElement(name = "fly_rings")
	public FlyRingData flyRingData;

	@XmlElement(name = "shields")
	public ShieldData shieldData;

	@XmlElement(name = "pets")
	public PetData petData;

	@XmlElement(name = "pet_feed")
	public PetFeedData petFeedData;

	@XmlElement(name = "dopings")
	public PetDopingData petDopingData;

	@XmlElement(name = "pet_buffs")
	public PetBuffsData petBuffsData;

	@XmlElement(name = "guides")
	public GuideHtmlData guideData;

	@XmlElement(name = "roads")
	public RoadData roadData;

	@XmlElement(name = "instance_cooltimes")
	public InstanceCooltimeData instanceCooltimeData;

	@XmlElement(name = "decomposable_items")
	public DecomposableItemsData decomposableItemsData;

	@XmlElement(name = "ai_templates")
	public AIData aiData;

	@XmlElement(name = "flypath_template")
	public FlyPathData flyPath;

	@XmlElement(name = "windstreams")
	public WindstreamData windstreamsData;

	@XmlElement(name = "item_restriction_cleanups")
	public ItemRestrictionCleanupData itemCleanup;

	@XmlElement(name = "assembled_npcs")
	public AssembledNpcsData assembledNpcData;

	@XmlElement(name = "cosmetic_items")
	public CosmeticItemsData cosmeticItemsData;

	@XmlElement(name = "auto_groups")
	public AutoGroupData autoGroupData;

	@XmlElement(name = "timed_events")
	public EventData eventData;

	@XmlElement(name = "spawns")
	public SpawnsData spawnsData;

	@XmlElement(name = "item_groups")
	public ItemGroupsData itemGroupsData;

	@XmlElement(name = "polymorph_panels")
	public PanelSkillsData panelSkillsData;

	@XmlElement(name = "instance_bonusattrs")
	public InstanceBuffData instanceBuffData;

	@XmlElement(name = "housing_objects")
	public HousingObjectData housingObjectData;

	@XmlElement(name = "rides")
	public RideData rideData;

	@XmlElement(name = "instance_exits")
	public InstanceExitData instanceExitData;

	@XmlElement(name = "portal_locs")
	public PortalLocData portalLocData;

	@XmlElement(name = "portal_templates2")
	public Portal2Data portalTemplate2;

	@XmlElement(name = "house_lands")
	public HouseData houseData;

	@XmlElement(name = "buildings")
	public HouseBuildingData houseBuildingData;

	@XmlElement(name = "house_parts")
	public HousePartsData housePartsData;

	@XmlElement(name = "curing_objects")
	public CuringObjectsData curingObjectsData;

	@XmlElement(name = "house_npcs")
	public HouseNpcsData houseNpcsData;

	@XmlElement(name = "assembly_items")
	public AssemblyItemsData assemblyItemData;

	@XmlElement(name = "mails")
	public Mails systemMailTemplates;

	@XmlElement(name = "material_templates")
	public MaterialData materiaData;

	@XmlElement(name = "challenge_tasks")
	public ChallengeData challengeData;

	@XmlElement(name = "conqueror_protector_ranks")
	public ConquerorAndProtectorData conquerorAndProtectorData;

	@XmlElement(name = "town_spawns_data")
	public TownSpawnsData townSpawnsData;

	@XmlElement(name = "skill_charge")
	public SkillChargeData skillChargeData;

	@XmlElement(name = "walker_versions")
	public WalkerVersionsData walkerVersionsData;

	@XmlElement(name = "tempering_templates")
	public TemperingData temperingData;

	@XmlElement(name = "enchant_templates")
	public EnchantData enchantData;

	@XmlElement(name = "global_rules")
	public GlobalDropData globalDropData;

	@XmlElement(name = "global_npc_exclusions")
	public GlobalNpcExclusionData globalExclusionData;

	@XmlElement(name = "multi_return_item")
	public MultiReturnItemData multiReturnItem;

	@XmlElement(name = "hotspot_template")
	public HotspotData hotspotData;

	@XmlElement(name = "item_purifications")
	public ItemPurificationData itemPurificationData;

	@XmlElement(name = "arcadelist")
	public UpgradeArcadeData upgradeArcadeData;

	@XmlElement(name = "login_events")
	public AtreianPassportData atreianPassportData;

	@XmlElement(name = "world_raid_locations")
	public WorldRaidData worldRaidData;

	@XmlElement(name = "kill_bounties")
	public KillBountyData killBountyData;

	@XmlElement(name = "legion_dominion_template")
	public LegionDominionData legionDominionData;

	@XmlElement(name = "alias_locations")
	public SkillAliasLocationData skillAliasLocationData;

	@XmlElement(name = "signet_data_templates")
	public SignetDataTemplates signetDataTemplates;

	@XmlTransient
	private List<Future<?>> afterUnmarshalTasks;
	@XmlTransient
	private Future<?> validationTask;

	void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {
		unmarshaller.setListener(new StaticDataListener(this));
	}

	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		Logger log = LoggerFactory.getLogger(StaticData.class);
		log.info("Loaded " + worldMapsData.size() + " maps");
		log.info("Loaded " + materiaData.size() + " material ids");
		log.info("Loaded weather for " + mapWeatherData.size() + " maps");
		log.info("Loaded " + playerExperienceTable.getMaxLevel() + " player experience table entries");
		log.info("Loaded " + absoluteStatsData.size() + " absolute stat templates");
		log.info("Loaded " + itemCleanup.size() + " item cleanup entries");
		log.info("Loaded " + itemData.size() + " item templates");
		log.info("Loaded " + itemRandomBonuses.size() + " item bonus templates");
		log.info("Loaded " + itemGroupsData.bonusSize() + " bonus item group templates and " + itemGroupsData.petFoodSize() + " pet food items");
		log.info("Loaded " + npcData.size() + " npc templates");
		log.info("Loaded " + customNpcDrop.size() + " custom npc drops");
		log.info("Loaded " + systemMailTemplates.size() + " system mail templates");
		log.info("Loaded " + npcShoutData.size() + " npc shout templates");
		log.info("Loaded " + petData.size() + " pet templates and " + petFeedData.size() + " food flavours");
		log.info("Loaded " + petDopingData.size() + " pet doping templates");
		log.info("Loaded " + petBuffsData.size() + " pet buffs templates");
		log.info("Loaded " + playerInitialData.size() + " initial player templates");
		log.info("Loaded " + tradeListData.size() + " trade lists");
		log.info("Loaded " + teleporterData.size() + " npc teleporter templates");
		log.info("Loaded " + teleLocationData.size() + " teleport locations");
		log.info("Loaded " + skillData.size() + " skill templates");
		log.info("Loaded " + skillChargeData.size() + " skill charge entries");
		log.info("Loaded " + motionData.size() + " motion times");
		log.info("Loaded " + skillTreeData.size() + " skill learn entries");
		log.info("Loaded " + cubeExpandData.size() + " cube expand entries");
		log.info("Loaded " + warehouseExpandData.size() + " warehouse expand entries");
		log.info("Loaded " + bindPointData.size() + " bind point entries");
		log.info("Loaded " + questData.size() + " quest data entries");
		log.info("Loaded " + gatherableData.size() + " gatherable entries");
		log.info("Loaded " + titleData.size() + " title entries");
		log.info("Loaded " + walkerData.size() + " walker routes");
		log.info("Loaded " + walkerVersionsData.size() + " walker group variants");
		log.info("Loaded " + zoneData.size() + " zone entries");
		log.info("Loaded " + goodsListData.size() + " goodslist entries");
		log.info("Loaded " + tribeRelationsData.size() + " tribe relation entries");
		log.info("Loaded " + recipeData.size() + " recipe entries");
		log.info("Loaded " + chestData.size() + " chest locations");
		log.info("Loaded " + staticDoorData.size() + " static door locations");
		log.info("Loaded " + itemSetData.size() + " item set entries");
		log.info("Loaded " + npcFactionsData.size() + " npc factions");
		log.info("Loaded " + npcSkillData.size() + " npc skill list entries");
		log.info("Loaded " + petSkillData.size() + " pet skill list entries");
		log.info("Loaded " + siegeLocationData.size() + " siege location entries");
		log.info("Loaded " + vortexData.size() + " vortex entries");
		log.info("Loaded " + riftData.size() + " rift entries");
		log.info("Loaded " + baseData.size() + " base entries");
		log.info("Loaded " + flyRingData.size() + " fly ring entries");
		log.info("Loaded " + shieldData.size() + " shield entries");
		log.info("Loaded " + petData.size() + " pet entries");
		log.info("Loaded " + guideData.size() + " guide entries");
		log.info("Loaded " + roadData.size() + " road entries");
		log.info("Loaded " + instanceCooltimeData.size() + " instance cooltime entries");
		log.info("Loaded " + decomposableItemsData.size() + " decomposable items entries");
		log.info("Loaded " + aiData.size() + " ai templates");
		log.info("Loaded " + flyPath.size() + " flypath templates");
		log.info("Loaded " + windstreamsData.size() + " windstream entries");
		log.info("Loaded " + assembledNpcData.size() + " assembled npcs entries");
		log.info("Loaded " + cosmeticItemsData.size() + " cosmetic items entries");
		log.info("Loaded " + autoGroupData.size() + " auto group entries");
		log.info("Loaded " + spawnsData.size() + " spawn maps entries");
		log.info("Loaded " + eventData.size() + " events");
		log.info("Loaded " + panelSkillsData.size() + " skill panel entries");
		log.info("Loaded " + instanceBuffData.size() + " instance Buffs entries");
		log.info("Loaded " + housingObjectData.size() + " housing object entries");
		log.info("Loaded " + rideData.size() + " ride info entries");
		log.info("Loaded " + instanceExitData.size() + " instance exit entries");
		log.info("Loaded " + portalLocData.size() + " portal loc entries");
		log.info("Loaded " + portalTemplate2.size() + " portal templates2 entries");
		log.info("Loaded " + houseData.size() + " housing lands");
		log.info("Loaded " + houseBuildingData.size() + " house building styles");
		log.info("Loaded " + housePartsData.size() + " house parts");
		log.info("Loaded " + houseNpcsData.size() + " house spawns");
		log.info("Loaded " + curingObjectsData.size() + " curing object entries");
		log.info("Loaded " + assemblyItemData.size() + " assembly items entries");
		log.info("Loaded " + challengeData.size() + " challenge tasks entries");
		log.info("Loaded " + conquerorAndProtectorData.size() + " conqueror and protector entries");
		log.info("Loaded " + townSpawnsData.getSpawnsCount() + " town spawns");
		log.info("Loaded " + temperingData.size() + " temperings");
		log.info("Loaded " + enchantData.size() + " enchants");
		log.info("Loaded " + globalDropData.size() + " global drop rules" + (globalExclusionData.isEmpty() ? "" : " with global drop npc exclusions"));
		log.info("Loaded " + multiReturnItem.size() + " multi return item entries");
		log.info("Loaded " + hotspotData.size() + " hotspot entries");
		log.info("Loaded " + itemPurificationData.size() + " item purifications entries");
		log.info("Loaded " + upgradeArcadeData.size() + " upgrade arcade entries");
		log.info("Loaded " + atreianPassportData.size() + " atreian passports");
		log.info("Loaded " + worldRaidData.size() + " world raid locations");
		log.info("Loaded " + killBountyData.size() + " kill bounty templates");
		log.info("Loaded " + legionDominionData.size() + " legion dominion locations");
		log.info("Loaded " + skillAliasLocationData.size() + " skill alias locations");
		log.info("Loaded " + signetDataTemplates.size() + " signet data templates");
	}

	public void setValidationTask(Future<?> validationTask) {
		this.validationTask = validationTask;
	}

	Future<?> getValidationTask() {
		return validationTask;
	}

	public void addAfterUnmarshalTask(Future<?> task) {
		if (afterUnmarshalTasks == null)
			afterUnmarshalTasks = new ArrayList<>();
		afterUnmarshalTasks.add(task);
	}

	void waitForAfterUnmarshalTasksToFinish() {
		if (afterUnmarshalTasks == null)
			return;
		afterUnmarshalTasks.forEach(task -> {
			try {
				task.get();
			} catch (InterruptedException | CancellationException ignored) {
			} catch (ExecutionException e) {
				throw e.getCause() instanceof Error ? (Error) e.getCause() : new GameServerError(e.getCause());
			}
		});
		afterUnmarshalTasks = null;
	}
}
