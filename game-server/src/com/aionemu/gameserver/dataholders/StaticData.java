package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

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

	@XmlElement(name = "pvp_exp_table")
	public PvpExpTable pvpExpTable;

	@XmlElement(name = "pvp_exp_mod_table")
	public PvpExpModTable pvpExpModTable;

	@XmlElement(name = "party_exp_mod_table")
	public PartyExpModTable partyExpModTable;

	@XmlElement(name = "mentee_exp_limit_table")
	public MenteeExpLimitTable menteeExpLimitTable;

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
		log.info("Loaded {} maps", worldMapsData.size());
		log.info("Loaded {} material ids", materiaData.size());
		log.info("Loaded weather for {} maps", mapWeatherData.size());
		log.info("Loaded {} player experience table entries", playerExperienceTable.getMaxLevel());
		log.info("Loaded {} pvp experience table entries", pvpExpTable.getMaxLevel());
		log.info("Loaded {} pvp experience modifier entries", pvpExpModTable.size());
		log.info("Loaded {} party experience modifier entries", partyExpModTable.size());
		log.info("Loaded {} mentee experience limit entries", menteeExpLimitTable.size());
		log.info("Loaded {} absolute stat templates", absoluteStatsData.size());
		log.info("Loaded {} item cleanup entries", itemCleanup.size());
		log.info("Loaded {} item templates", itemData.size());
		log.info("Loaded {} item bonus templates", itemRandomBonuses.size());
		log.info("Loaded {} bonus item group templates and {} pet food items", itemGroupsData.bonusSize(), itemGroupsData.petFoodSize());
		log.info("Loaded {} npc templates", npcData.size());
		log.info("Loaded {} custom npc drops", customNpcDrop.size());
		log.info("Loaded {} system mail templates", systemMailTemplates.size());
		log.info("Loaded {} npc shout templates", npcShoutData.size());
		log.info("Loaded {} pet templates and {} food flavours", petData.size(), petFeedData.size());
		log.info("Loaded {} pet doping templates", petDopingData.size());
		log.info("Loaded {} pet buffs templates", petBuffsData.size());
		log.info("Loaded {} initial player templates", playerInitialData.size());
		log.info("Loaded {} trade lists", tradeListData.size());
		log.info("Loaded {} npc teleporter templates", teleporterData.size());
		log.info("Loaded {} teleport locations", teleLocationData.size());
		log.info("Loaded {} skill templates", skillData.size());
		log.info("Loaded {} skill charge entries", skillChargeData.size());
		log.info("Loaded {} motion times", motionData.size());
		log.info("Loaded {} skill learn entries", skillTreeData.size());
		log.info("Loaded {} cube expand entries", cubeExpandData.size());
		log.info("Loaded {} warehouse expand entries", warehouseExpandData.size());
		log.info("Loaded {} bind point entries", bindPointData.size());
		log.info("Loaded {} quest data entries", questData.size());
		log.info("Loaded {} gatherable entries", gatherableData.size());
		log.info("Loaded {} title entries", titleData.size());
		log.info("Loaded {} walker routes", walkerData.size());
		log.info("Loaded {} walker group variants", walkerVersionsData.size());
		log.info("Loaded {} zone entries", zoneData.size());
		log.info("Loaded {} goodslist entries", goodsListData.size());
		log.info("Loaded {} tribe relation entries", tribeRelationsData.size());
		log.info("Loaded {} recipe entries", recipeData.size());
		log.info("Loaded {} chest locations", chestData.size());
		log.info("Loaded {} static door locations", staticDoorData.size());
		log.info("Loaded {} item set entries", itemSetData.size());
		log.info("Loaded {} npc factions", npcFactionsData.size());
		log.info("Loaded {} npc skill list entries", npcSkillData.size());
		log.info("Loaded {} pet skill list entries", petSkillData.size());
		log.info("Loaded {} siege location entries", siegeLocationData.size());
		log.info("Loaded {} vortex entries", vortexData.size());
		log.info("Loaded {} rift entries", riftData.size());
		log.info("Loaded {} base entries", baseData.size());
		log.info("Loaded {} fly ring entries", flyRingData.size());
		log.info("Loaded {} pet entries", petData.size());
		log.info("Loaded {} guide entries", guideData.size());
		log.info("Loaded {} road entries", roadData.size());
		log.info("Loaded {} instance cooltime entries", instanceCooltimeData.size());
		log.info("Loaded {} decomposable items entries", decomposableItemsData.size());
		log.info("Loaded {} ai templates", aiData.size());
		log.info("Loaded {} flypath templates", flyPath.size());
		log.info("Loaded {} windstream entries", windstreamsData.size());
		log.info("Loaded {} assembled npcs entries", assembledNpcData.size());
		log.info("Loaded {} cosmetic items entries", cosmeticItemsData.size());
		log.info("Loaded {} auto group entries", autoGroupData.size());
		log.info("Loaded {} spawn maps entries", spawnsData.size());
		log.info("Loaded {} events", eventData.size());
		log.info("Loaded {} skill panel entries", panelSkillsData.size());
		log.info("Loaded {} instance Buffs entries", instanceBuffData.size());
		log.info("Loaded {} housing object entries", housingObjectData.size());
		log.info("Loaded {} ride info entries", rideData.size());
		log.info("Loaded {} instance exit entries", instanceExitData.size());
		log.info("Loaded {} portal loc entries", portalLocData.size());
		log.info("Loaded {} portal templates2 entries", portalTemplate2.size());
		log.info("Loaded {} housing lands", houseData.size());
		log.info("Loaded {} house building styles", houseBuildingData.size());
		log.info("Loaded {} house parts", housePartsData.size());
		log.info("Loaded {} house spawns", houseNpcsData.size());
		log.info("Loaded {} assembly items entries", assemblyItemData.size());
		log.info("Loaded {} challenge tasks entries", challengeData.size());
		log.info("Loaded {} conqueror and protector entries", conquerorAndProtectorData.size());
		log.info("Loaded {} town spawns", townSpawnsData.getSpawnsCount());
		log.info("Loaded {} temperings", temperingData.size());
		log.info("Loaded {} enchants", enchantData.size());
		log.info("Loaded {} global drop rules{}", globalDropData.size(), globalExclusionData.isEmpty() ? "" : " with global drop npc exclusions");
		log.info("Loaded {} multi return item entries", multiReturnItem.size());
		log.info("Loaded {} hotspot entries", hotspotData.size());
		log.info("Loaded {} item purifications entries", itemPurificationData.size());
		log.info("Loaded {} upgrade arcade entries", upgradeArcadeData.size());
		log.info("Loaded {} atreian passports", atreianPassportData.size());
		log.info("Loaded {} world raid locations", worldRaidData.size());
		log.info("Loaded {} kill bounty templates", killBountyData.size());
		log.info("Loaded {} legion dominion locations", legionDominionData.size());
		log.info("Loaded {} skill alias locations", skillAliasLocationData.size());
		log.info("Loaded {} signet data templates", signetDataTemplates.size());
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
