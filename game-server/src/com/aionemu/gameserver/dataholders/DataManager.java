package com.aionemu.gameserver.dataholders;

import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.dataholders.loadingutils.XmlDataLoader;
import com.aionemu.gameserver.model.templates.item.actions.DecomposeAction;
import com.aionemu.gameserver.model.templates.mail.Mails;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;

/**
 * This class is holding whole static data, that is loaded from /data/static_data directory.<br>
 * The data is loaded by XMLDataLoader using JAXB.<br>
 * <br>
 * This class temporarily also contains data loaded from txt files by DataLoaders. It'll be changed later.
 *
 * @author Luno , orz, Wakizashi, Neon
 */
public final class DataManager {

	private static final Logger log = LoggerFactory.getLogger(DataManager.class);
	public static AbsoluteStatsData ABSOLUTE_STATS_DATA;
	public static AIData AI_DATA;
	public static UpgradeArcadeData UPGRADE_ARCADE_DATA;
	public static AssembledNpcsData ASSEMBLED_NPC_DATA;
	public static AssemblyItemsData ASSEMBLY_ITEM_DATA;
	public static AtreianPassportData ATREIAN_PASSPORT_DATA;
	public static AutoGroupData AUTO_GROUP;
	public static BaseData BASE_DATA;
	public static BindPointData BIND_POINT_DATA;
	public static ChallengeData CHALLENGE_DATA;
	public static ChestData CHEST_DATA;
	public static CosmeticItemsData COSMETIC_ITEMS_DATA;
	public static CubeExpandData CUBEEXPANDER_DATA;
	public static CuringObjectsData CURING_OBJECTS_DATA;
	public static CustomDrop CUSTOM_NPC_DROP;
	public static DecomposableItemsData DECOMPOSABLE_ITEMS_DATA;
	public static EnchantData ENCHANT_DATA;
	public static EventData EVENT_DATA;
	public static FlyPathData FLY_PATH;
	public static FlyRingData FLY_RING_DATA;
	public static GuideHtmlData GUIDE_HTML_DATA;
	public static ItemData ITEM_DATA;
	public static ItemRandomBonusData ITEM_RANDOM_BONUSES;
	public static ItemSetData ITEM_SET_DATA;
	public static NpcData NPC_DATA;
	public static GatherableData GATHERABLE_DATA;
	public static GlobalDropData GLOBAL_DROP_DATA;
	public static GlobalNpcExclusionData GLOBAL_EXCLUSION_DATA;
	public static GoodsListData GOODSLIST_DATA;
	public static HotspotData HOTSPOT_DATA;
	public static HouseData HOUSE_DATA;
	public static HouseBuildingData HOUSE_BUILDING_DATA;
	public static HouseNpcsData HOUSE_NPCS_DATA;
	public static HousePartsData HOUSE_PARTS_DATA;
	public static HousingObjectData HOUSING_OBJECT_DATA;
	public static KillBountyData KILL_BOUNTY_DATA;
	public static InstanceBuffData INSTANCE_BUFF_DATA;
	public static InstanceCooltimeData INSTANCE_COOLTIME_DATA;
	public static InstanceExitData INSTANCE_EXIT_DATA;
	public static ItemGroupsData ITEM_GROUPS_DATA;
	public static ItemPurificationData ITEM_PURIFICATION_DATA;
	public static ItemRestrictionCleanupData ITEM_CLEAN_UP;
	public static Mails SYSTEM_MAIL_TEMPLATES;
	public static MapWeatherData MAP_WEATHER_DATA;
	public static MaterialData MATERIAL_DATA;
	public static MotionData MOTION_DATA;
	public static MultiReturnItemData MULTIRETURN_DATA;
	public static NpcFactionsData NPC_FACTIONS_DATA;
	public static NpcShoutData NPC_SHOUT_DATA;
	public static NpcSkillData NPC_SKILL_DATA;
	public static PanelSkillsData PANEL_SKILL_DATA;
	public static PetData PET_DATA;
	public static PetBuffsData PET_BUFFS_DATA;
	public static PetDopingData PET_DOPING_DATA;
	public static PetFeedData PET_FEED_DATA;
	public static PetSkillData PET_SKILL_DATA;
	public static PlayerExperienceTable PLAYER_EXPERIENCE_TABLE;
	public static PlayerInitialData PLAYER_INITIAL_DATA;
	public static Portal2Data PORTAL2_DATA;
	public static PortalLocData PORTAL_LOC_DATA;
	public static QuestsData QUEST_DATA;
	public static RecipeData RECIPE_DATA;
	public static RideData RIDE_DATA;
	public static RiftData RIFT_DATA;
	public static RoadData ROAD_DATA;
	public static ConquerorAndProtectorData CONQUEROR_AND_PROTECTOR_DATA;
	public static ShieldData SHIELD_DATA;
	public static SiegeLocationData SIEGE_LOCATION_DATA;
	public static SkillChargeData SKILL_CHARGE_DATA;
	public static SkillData SKILL_DATA;
	public static SkillTreeData SKILL_TREE_DATA;
	public static SpawnsData SPAWNS_DATA;
	public static StaticDoorData STATICDOOR_DATA;
	public static TeleLocationData TELELOCATION_DATA;
	public static TeleporterData TELEPORTER_DATA;
	public static TemperingData TEMPERING_DATA;
	public static TitleData TITLE_DATA;
	public static TownSpawnsData TOWN_SPAWNS_DATA;
	public static TradeListData TRADE_LIST_DATA;
	public static TribeRelationsData TRIBE_RELATIONS_DATA;
	public static VortexData VORTEX_DATA;
	public static WalkerData WALKER_DATA;
	public static WalkerVersionsData WALKER_VERSIONS_DATA;
	public static WarehouseExpandData WAREHOUSEEXPANDER_DATA;
	public static WindstreamData WINDSTREAM_DATA;
	public static WorldMapsData WORLD_MAPS_DATA;
	public static WorldRaidData WORLD_RAID_DATA;
	public static XMLQuests XML_QUESTS;
	public static ZoneData ZONE_DATA;
	public static LegionDominionData LEGION_DOMINION_DATA;
	public static SkillAliasLocationData SKILL_ALIAS_LOCATION_DATA;
	public static SignetDataTemplates SIGNET_DATA_TEMPLATES;
	private static Future<?> xmlValidationTask;

	/**
	 * Constructor creating <tt>DataManager</tt> instance.<br>
	 * NOTICE: calling constructor implies loading whole data from /data/static_data immediately
	 */
	public static DataManager getInstance() {
		return SingletonHolder.instance;
	}

	private DataManager() {
		long start = System.currentTimeMillis();
		StaticData data = XmlDataLoader.loadStaticData();
		NPC_DATA = data.npcData;
		CUSTOM_NPC_DROP = data.customNpcDrop;
		WORLD_MAPS_DATA = data.worldMapsData;
		MATERIAL_DATA = data.materiaData;
		MAP_WEATHER_DATA = data.mapWeatherData;
		PLAYER_EXPERIENCE_TABLE = data.playerExperienceTable;
		ABSOLUTE_STATS_DATA = data.absoluteStatsData;
		ITEM_CLEAN_UP = data.itemCleanup;
		ITEM_DATA = data.itemData;
		ITEM_RANDOM_BONUSES = data.itemRandomBonuses;
		NPC_SHOUT_DATA = data.npcShoutData;
		GATHERABLE_DATA = data.gatherableData;
		PLAYER_INITIAL_DATA = data.playerInitialData;
		SKILL_DATA = data.skillData;
		SKILL_CHARGE_DATA = data.skillChargeData;
		MOTION_DATA = data.motionData;
		SKILL_TREE_DATA = data.skillTreeData;
		TITLE_DATA = data.titleData;
		TRADE_LIST_DATA = data.tradeListData;
		TELEPORTER_DATA = data.teleporterData;
		TELELOCATION_DATA = data.teleLocationData;
		CUBEEXPANDER_DATA = data.cubeExpandData;
		WAREHOUSEEXPANDER_DATA = data.warehouseExpandData;
		BIND_POINT_DATA = data.bindPointData;
		QUEST_DATA = data.questData;
		XML_QUESTS = data.questsScriptData;
		ZONE_DATA = data.zoneData;
		WALKER_DATA = data.walkerData;
		WALKER_VERSIONS_DATA = data.walkerVersionsData;
		GOODSLIST_DATA = data.goodsListData;
		TRIBE_RELATIONS_DATA = data.tribeRelationsData;
		RECIPE_DATA = data.recipeData;
		CHEST_DATA = data.chestData;
		STATICDOOR_DATA = data.staticDoorData;
		ITEM_SET_DATA = data.itemSetData;
		NPC_FACTIONS_DATA = data.npcFactionsData;
		NPC_SKILL_DATA = data.npcSkillData;
		PET_SKILL_DATA = data.petSkillData;
		SIEGE_LOCATION_DATA = data.siegeLocationData;
		VORTEX_DATA = data.vortexData;
		RIFT_DATA = data.riftData;
		BASE_DATA = data.baseData;
		FLY_RING_DATA = data.flyRingData;
		SHIELD_DATA = data.shieldData;
		PET_DATA = data.petData;
		PET_FEED_DATA = data.petFeedData;
		PET_DOPING_DATA = data.petDopingData;
		PET_BUFFS_DATA = data.petBuffsData;
		GUIDE_HTML_DATA = data.guideData;
		ROAD_DATA = data.roadData;
		INSTANCE_COOLTIME_DATA = data.instanceCooltimeData;
		DECOMPOSABLE_ITEMS_DATA = data.decomposableItemsData;
		AI_DATA = data.aiData;
		FLY_PATH = data.flyPath;
		WINDSTREAM_DATA = data.windstreamsData;
		ASSEMBLED_NPC_DATA = data.assembledNpcData;
		COSMETIC_ITEMS_DATA = data.cosmeticItemsData;
		SPAWNS_DATA = data.spawnsData;
		ITEM_GROUPS_DATA = data.itemGroupsData;
		ASSEMBLY_ITEM_DATA = data.assemblyItemData;
		AUTO_GROUP = data.autoGroupData;
		EVENT_DATA = data.eventData;
		PANEL_SKILL_DATA = data.panelSkillsData;
		INSTANCE_BUFF_DATA = data.instanceBuffData;
		HOUSING_OBJECT_DATA = data.housingObjectData;
		RIDE_DATA = data.rideData;
		INSTANCE_EXIT_DATA = data.instanceExitData;
		PORTAL_LOC_DATA = data.portalLocData;
		PORTAL2_DATA = data.portalTemplate2;
		HOUSE_DATA = data.houseData;
		HOUSE_BUILDING_DATA = data.houseBuildingData;
		HOUSE_PARTS_DATA = data.housePartsData;
		CURING_OBJECTS_DATA = data.curingObjectsData;
		HOUSE_NPCS_DATA = data.houseNpcsData;
		SYSTEM_MAIL_TEMPLATES = data.systemMailTemplates;
		CHALLENGE_DATA = data.challengeData;
		TOWN_SPAWNS_DATA = data.townSpawnsData;
		CONQUEROR_AND_PROTECTOR_DATA = data.conquerorAndProtectorData;
		ENCHANT_DATA = data.enchantData;
		TEMPERING_DATA = data.temperingData;
		GLOBAL_DROP_DATA = data.globalDropData;
		GLOBAL_EXCLUSION_DATA = data.globalExclusionData;
		MULTIRETURN_DATA = data.multiReturnItem;
		HOTSPOT_DATA = data.hotspotData;
		ITEM_PURIFICATION_DATA = data.itemPurificationData;
		UPGRADE_ARCADE_DATA = data.upgradeArcadeData;
		ATREIAN_PASSPORT_DATA = data.atreianPassportData;
		WORLD_RAID_DATA = data.worldRaidData;
		KILL_BOUNTY_DATA = data.killBountyData;
		LEGION_DOMINION_DATA = data.legionDominionData;
		SKILL_ALIAS_LOCATION_DATA = data.skillAliasLocationData;
		SIGNET_DATA_TEMPLATES = data.signetDataTemplates;
		xmlValidationTask = data.getValidationTask();
		data.waitForAfterUnmarshalTasksToFinish();

		// subsequent data processing (must be called after initializing DataManager fields)
		ITEM_DATA.cleanup();
		Collection<NpcTemplate> npcTemplates = NPC_DATA.getNpcData();
		GLOBAL_DROP_DATA.processRules(npcTemplates);
		TRADE_LIST_DATA.validateBuyLists(npcTemplates);
		SKILL_DATA.validateMotions();
		DecomposeAction.validateRandomItemIds();

		long time = System.currentTimeMillis() - start;
		log.info("##### [Static Data loaded in " + String.format("%.1f", time / 1000f) + " seconds] #####");
	}

	public static void waitForValidationToFinishAndShutdownOnFail() {
		if (xmlValidationTask == null)
			return;
		try {
			xmlValidationTask.get();
		} catch (InterruptedException | CancellationException ignored) {
		} catch (ExecutionException e) {
			throw e.getCause() instanceof Error ? (Error) e.getCause() : new GameServerError(e.getCause());
		} finally {
			xmlValidationTask = null;
		}
	}

	private static class SingletonHolder {

		protected static final DataManager instance = new DataManager();
	}

}
