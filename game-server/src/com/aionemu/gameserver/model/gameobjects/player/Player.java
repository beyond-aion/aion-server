package com.aionemu.gameserver.model.gameobjects.player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.controllers.FlyController;
import com.aionemu.gameserver.controllers.PlayerController;
import com.aionemu.gameserver.controllers.attack.AggroList;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.attack.PlayerAggroList;
import com.aionemu.gameserver.controllers.effect.PlayerEffectController;
import com.aionemu.gameserver.controllers.movement.PlayerMoveController;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.dao.PlayerVarsDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TribeClass;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.actions.PlayerActions;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.animations.ArrivalAnimation;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank.AbyssRankUpdateType;
import com.aionemu.gameserver.model.gameobjects.player.emotion.EmotionList;
import com.aionemu.gameserver.model.gameobjects.player.motion.MotionList;
import com.aionemu.gameserver.model.gameobjects.player.npcFaction.NpcFactions;
import com.aionemu.gameserver.model.gameobjects.player.title.TitleList;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.model.gameobjects.state.FlyState;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.ingameshop.InGameShop;
import com.aionemu.gameserver.model.items.ItemCooldown;
import com.aionemu.gameserver.model.items.storage.IStorage;
import com.aionemu.gameserver.model.items.storage.LegionStorageProxy;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.aionemu.gameserver.model.stats.container.PlayerGameStats;
import com.aionemu.gameserver.model.stats.container.PlayerLifeStats;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceGroup;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.templates.flypath.FlyPathEntry;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.item.ItemUseLimits;
import com.aionemu.gameserver.model.templates.ride.RideInfo;
import com.aionemu.gameserver.model.templates.windstreams.WindstreamPath;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.DuelService;
import com.aionemu.gameserver.services.ExchangeService;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.skillengine.condition.ChainCondition;
import com.aionemu.gameserver.skillengine.effect.RebirthEffect;
import com.aionemu.gameserver.skillengine.model.ChainSkills;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.task.CraftingTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * This class is representing Player object, it contains all needed data.
 *
 * @author -Nemesiss-, SoulKeeper, alexa026, cura
 */
public class Player extends Creature {

	public volatile RideInfo ride;
	public volatile InRoll inRoll;
	public volatile WindstreamPath windstreamPath;
	public InGameShop inGameShop;
	private final PlayerAccountData playerAccountData;
	private final Account playerAccount;
	private LegionMember legionMember;

	private MacroList macroList;
	private PlayerSkillList skillList;
	private FriendList friendList;
	private BlockList blockList;
	private PetList toyPetList;
	private Mailbox mailbox;
	private PrivateStore store;
	private TitleList titleList;
	private QuestStateList questStateList;
	private RecipeList recipeList;
	private List<House> houses;

	private ResponseRequester requester;
	private boolean lookingForGroup = false;
	private Storage inventory;
	private Storage[] petBag = new Storage[StorageType.PET_BAG_MAX - StorageType.PET_BAG_MIN + 1];
	private Storage[] cabinets = new Storage[StorageType.HOUSE_WH_MAX - StorageType.HOUSE_WH_MIN + 1];
	private Storage regularWarehouse;
	private Equipment equipment;

	private final AbsoluteStatOwner absStatsHolder;
	private PlayerSettings playerSettings;

	private PlayerGroup playerGroup;
	private PlayerAllianceGroup playerAllianceGroup;

	private AbyssRank abyssRank;
	private NpcFactions npcFactions;

	private int flyState = 0;
	private FlyController flyController;
	private CraftingTask craftingTask;
	private int flightTeleportId;
	private int flightDistance;
	private Summon summon;
	private Pet pet;
	private Kisk kisk;
	private boolean isResByPlayer = false;
	private int resurrectionSkill = 0;
	private boolean isFlyingBeforeDeath = false;
	private Npc postman = null;
	private boolean isInResurrectPosState = false;
	private float resPosX = 0;
	private float resPosY = 0;
	private float resPosZ = 0;

	private int abyssRankListUpdateMask = 0;

	private BindPointPosition bindPoint;

	private final Map<Integer, ItemCooldown> itemCoolDowns = new ConcurrentHashMap<>();
	private final PortalCooldownList portalCooldownList;
	private final Cooldowns craftCooldowns;
	private final Cooldowns houseObjectCooldowns;
	private long nextSkillUse;
	private ChainSkills chainSkills;
	private Map<AttackStatus, Long> lastCounterSkill = new HashMap<>();

	private long prisonEndTimeMillis = 0;
	private long gatherRestrictionMillis;
	private String captchaWord;
	private byte[] captchaImage;

	/**
	 * Connection of this Player.
	 */
	private AionConnection clientConnection;
	private FlyPathEntry flyLocationId;
	private long flyStartTime;

	private EmotionList emotions;
	private MotionList motions;

	private long flyReuseTime;

	private boolean isMentor;

	private long lastMsgTime = 0;
	private int floodMsgCount = 0;

	private int lootingNpcOid;
	private RebirthEffect rebirthEffect;

	// Needed to remove supplements queue
	private int subtractedSupplementsCount;
	private int subtractedSupplementId;
	private byte portAnimation;
	private boolean isInSprintMode;
	private List<ActionObserver> rideObservers;

	private int battleReturnMap;
	private float[] battleReturnCoords;
	private int robotId;
	private boolean isInFfaTeamMode;
	private int customStates;

	private AtomicInteger fearCount = new AtomicInteger(), sleepCount = new AtomicInteger(), paralyzeCount = new AtomicInteger();
	private AtomicLong cumulativeFearResistExpirationTime = new AtomicLong(), cumulativeSleepResistExpirationTime = new AtomicLong(), cumulativeParalyzeResistExpirationTime = new AtomicLong();

	public Player(PlayerAccountData playerAccountData, Account account) {
		super(playerAccountData.getPlayerCommonData().getPlayerObjId(), new PlayerController(), null, playerAccountData.getPlayerCommonData(),
			playerAccountData.getPlayerCommonData().getPosition(), false);
		this.playerAccountData = playerAccountData;
		this.playerAccount = account;

		this.requester = new ResponseRequester(this);
		this.questStateList = new QuestStateList();
		this.titleList = new TitleList();
		this.portalCooldownList = new PortalCooldownList(this);
		this.craftCooldowns = new Cooldowns();
		this.houseObjectCooldowns = new Cooldowns();
		this.toyPetList = new PetList(this);
		getController().setOwner(this);
		moveController = new PlayerMoveController(this);

		setGameStats(new PlayerGameStats(this));
		setLifeStats(new PlayerLifeStats(this));
		inGameShop = new InGameShop();
		absStatsHolder = new AbsoluteStatOwner(this, 0);
	}

	public boolean isInPlayerMode(PlayerMode mode) {
		return PlayerActions.isInPlayerMode(this, mode);
	}

	public void setPlayerMode(PlayerMode mode, Object obj) {
		PlayerActions.setPlayerMode(this, mode, obj);
	}

	public void unsetPlayerMode(PlayerMode mode) {
		PlayerActions.unsetPlayerMode(this, mode);
	}

	@Override
	public PlayerMoveController getMoveController() {
		return (PlayerMoveController) super.getMoveController();
	}

	@Override
	protected final AggroList createAggroList() {
		return new PlayerAggroList(this);
	}

	public PlayerCommonData getCommonData() {
		return playerAccountData.getPlayerCommonData();
	}

	@Override
	public final String getName() {
		return getName(false);
	}

	public String getName(boolean displayCustomTag) {
		if (displayCustomTag && AdminConfig.NAME_TAGS.length > 0) {
			int index = playerAccount.getAccessLevel() - 1;
			if (index >= 0 && index < AdminConfig.NAME_TAGS.length)
				return String.format(AdminConfig.NAME_TAGS[index], getCommonData().getName());
		}
		return getCommonData().getName();
	}

	public PlayerAppearance getPlayerAppearance() {
		return playerAccountData.getAppearance();
	}

	public void setPlayerAppearance(PlayerAppearance playerAppearance) {
		playerAccountData.setAppearance(playerAppearance);
	}

	/**
	 * Set connection of this player.
	 *
	 * @param clientConnection
	 */
	public void setClientConnection(AionConnection clientConnection) {
		this.clientConnection = clientConnection;
	}

	/**
	 * Get connection of this player.
	 *
	 * @return AionConnection of this player.
	 */
	public AionConnection getClientConnection() {
		return clientConnection;
	}

	public MacroList getMacroList() {
		return macroList;
	}

	public void setMacroList(MacroList macroList) {
		this.macroList = macroList;
	}

	public PlayerSkillList getSkillList() {
		return skillList;
	}

	public void setSkillList(PlayerSkillList skillList) {
		this.skillList = skillList;
	}

	public Pet getPet() {
		return pet;
	}

	public void setPet(Pet pet) {
		this.pet = pet;
	}

	/**
	 * Gets this players Friend List
	 *
	 * @return FriendList
	 */
	public FriendList getFriendList() {
		return friendList;
	}

	/**
	 * Is this player looking for a group
	 *
	 * @return true or false
	 */
	public boolean isLookingForGroup() {
		return lookingForGroup;
	}

	/**
	 * Sets whether or not this player is looking for a group
	 *
	 * @param lookingForGroup
	 */
	public void setLookingForGroup(boolean lookingForGroup) {
		this.lookingForGroup = lookingForGroup;
	}

	public boolean isInAttackMode() {
		return isInState(CreatureState.WEAPON_EQUIPPED);
	}

	public boolean isGatherRestricted() {
		return getGatherRestrictionDurationSeconds() > 0;
	}

	public void setGatherRestrictionExpirationTime(long millis) {
		gatherRestrictionMillis = millis;
	}

	public int getGatherRestrictionDurationSeconds() {
		if (gatherRestrictionMillis == 0)
			return 0;
		int durationSeconds = (int) ((gatherRestrictionMillis - System.currentTimeMillis()) / 1000);
		if (durationSeconds < 0)
			gatherRestrictionMillis = durationSeconds = 0;
		return durationSeconds;
	}

	public String getCaptchaWord() {
		return captchaWord;
	}

	public void setCaptchaWord(String captchaWord) {
		this.captchaWord = captchaWord;
	}

	public byte[] getCaptchaImage() {
		return captchaImage;
	}

	public void setCaptchaImage(byte[] captchaImage) {
		this.captchaImage = captchaImage;
	}

	/**
	 * Sets this players friend list. <br />
	 * Remember to send the player the <tt>SM_FRIEND_LIST</tt> packet.
	 *
	 * @param list
	 */
	public void setFriendList(FriendList list) {
		this.friendList = list;
	}

	public BlockList getBlockList() {
		return blockList;
	}

	public void setBlockList(BlockList list) {
		this.blockList = list;
	}

	public final PetList getPetList() {
		return toyPetList;
	}

	@Override
	public PlayerLifeStats getLifeStats() {
		return (PlayerLifeStats) super.getLifeStats();
	}

	@Override
	public PlayerGameStats getGameStats() {
		return (PlayerGameStats) super.getGameStats();
	}

	/**
	 * Gets the ResponseRequester for this player
	 *
	 * @return ResponseRequester
	 */
	public ResponseRequester getResponseRequester() {
		return requester;
	}

	public boolean isOnline() {
		return getClientConnection() != null;
	}

	public int getQuestExpands() {
		return getCommonData().getQuestExpands();
	}

	public int getNpcExpands() {
		return getCommonData().getNpcExpands();
	}

	public int getItemExpands() {
		return getCommonData().getItemExpands();
	}

	public void setCubeLimit() {
		getInventory().setLimit(StorageType.CUBE.getLimit() + (getNpcExpands() + getQuestExpands() + getItemExpands()) * getInventory().getRowLength());
	}

	public PlayerClass getPlayerClass() {
		return getCommonData().getPlayerClass();
	}

	public Gender getGender() {
		return getCommonData().getGender();
	}

	/**
	 * Return PlayerController of this Player Object.
	 *
	 * @return PlayerController.
	 */
	@Override
	public PlayerController getController() {
		return (PlayerController) super.getController();
	}

	@Override
	public byte getLevel() {
		return (byte) getCommonData().getLevel();
	}

	/**
	 * @return the inventory
	 */

	public Equipment getEquipment() {
		return equipment;
	}

	public void setEquipment(Equipment equipment) {
		this.equipment = equipment;
	}

	/**
	 * @return the player private store
	 */
	public PrivateStore getStore() {
		return store;
	}

	/**
	 * @param store
	 *          the store that needs to be set
	 */
	public void setStore(PrivateStore store) {
		this.store = store;
	}

	/**
	 * @return the questStatesList
	 */
	public QuestStateList getQuestStateList() {
		return questStateList;
	}

	/**
	 * @param questStateList
	 *          the QuestStateList to set
	 */
	public void setQuestStateList(QuestStateList questStateList) {
		this.questStateList = questStateList;
	}

	public RecipeList getRecipeList() {
		return recipeList;
	}

	public void setRecipeList(RecipeList recipeList) {
		this.recipeList = recipeList;
	}

	/**
	 * @param storage
	 *          the storage to set (should be set right after player object is created)
	 */
	public void setStorage(Storage storage) {
		if (storage.getStorageType() == StorageType.CUBE)
			inventory = storage;
		else if (storage.getStorageType().getId() >= StorageType.PET_BAG_MIN && storage.getStorageType().getId() <= StorageType.PET_BAG_MAX)
			petBag[storage.getStorageType().getId() - StorageType.PET_BAG_MIN] = storage;
		else if (storage.getStorageType().getId() >= StorageType.HOUSE_WH_MIN && storage.getStorageType().getId() <= StorageType.HOUSE_WH_MAX)
			cabinets[storage.getStorageType().getId() - StorageType.HOUSE_WH_MIN] = storage;
		else if (storage.getStorageType() == StorageType.REGULAR_WAREHOUSE)
			regularWarehouse = storage;
		storage.setOwner(this);
	}

	/**
	 * @param storageType
	 * @return
	 */
	public IStorage getStorage(int storageType) {
		if (storageType == StorageType.CUBE.getId())
			return inventory;

		if (storageType == StorageType.REGULAR_WAREHOUSE.getId())
			return regularWarehouse;

		if (storageType == StorageType.ACCOUNT_WAREHOUSE.getId())
			return playerAccount.getAccountWarehouse();

		if (storageType == StorageType.LEGION_WAREHOUSE.getId() && getLegion() != null)
			return new LegionStorageProxy(getLegion().getLegionWarehouse(), this);

		if (storageType >= StorageType.PET_BAG_MIN && storageType <= StorageType.PET_BAG_MAX)
			return petBag[storageType - StorageType.PET_BAG_MIN];

		if (storageType >= StorageType.HOUSE_WH_MIN && storageType <= StorageType.HOUSE_WH_MAX)
			return cabinets[storageType - StorageType.HOUSE_WH_MIN];

		return null;
	}

	public Storage[] getPetBag() {
		return petBag;
	}

	/**
	 * Items from UPDATE_REQUIRED storages and equipment
	 *
	 * @return
	 */
	public List<Item> getDirtyItemsToUpdate() {
		List<Item> dirtyItems = new ArrayList<>();

		for (StorageType st : StorageType.values()) {
			IStorage storage = getStorage(st.getId());
			if (storage != null && storage.getPersistentState() == PersistentState.UPDATE_REQUIRED) {
				dirtyItems.addAll(storage.getItemsWithKinah());
				dirtyItems.addAll(storage.getDeletedItems());
				storage.setPersistentState(PersistentState.UPDATED);
			}
		}

		if (equipment.getPersistentState() == PersistentState.UPDATE_REQUIRED) {
			dirtyItems.addAll(equipment.getEquippedItems());
			equipment.setPersistentState(PersistentState.UPDATED);
		}

		return dirtyItems;
	}

	/**
	 * //TODO probably need to optimize here
	 *
	 * @return
	 */
	public List<Item> getAllItems() {
		List<Item> items = new ArrayList<>();
		items.addAll(inventory.getItemsWithKinah());
		items.addAll(regularWarehouse.getItemsWithKinah());
		items.addAll(playerAccount.getAccountWarehouse().getItemsWithKinah());

		for (int petBagId = StorageType.PET_BAG_MIN; petBagId <= StorageType.PET_BAG_MAX; petBagId++) {
			IStorage petBag = getStorage(petBagId);
			if (petBag != null)
				items.addAll(petBag.getItemsWithKinah());
		}

		for (int houseWhId = StorageType.HOUSE_WH_MIN; houseWhId <= StorageType.HOUSE_WH_MAX; houseWhId++) {
			IStorage cabinet = getStorage(houseWhId);
			if (cabinet != null)
				items.addAll(cabinet.getItemsWithKinah());
		}

		items.addAll(getEquipment().getEquippedItems());
		return items;
	}

	public Storage getInventory() {
		return inventory;
	}

	/**
	 * @return the playerSettings
	 */
	public PlayerSettings getPlayerSettings() {
		return playerSettings;
	}

	/**
	 * @param playerSettings
	 *          the playerSettings to set
	 */
	public void setPlayerSettings(PlayerSettings playerSettings) {
		this.playerSettings = playerSettings;
	}

	public TitleList getTitleList() {
		return titleList;
	}

	public void setTitleList(TitleList titleList) {
		this.titleList = titleList;
		titleList.setOwner(this);
	}

	public PlayerGroup getPlayerGroup() {
		return playerGroup;
	}

	public void setPlayerGroup(PlayerGroup playerGroup) {
		this.playerGroup = playerGroup;
	}

	/**
	 * @return the abyssRank
	 */
	public AbyssRank getAbyssRank() {
		return abyssRank;
	}

	/**
	 * @param abyssRank
	 *          the abyssRank to set
	 */
	public void setAbyssRank(AbyssRank abyssRank) {
		this.abyssRank = abyssRank;
	}

	@Override
	public PlayerEffectController getEffectController() {
		return (PlayerEffectController) super.getEffectController();
	}

	/**
	 * Returns true if has valid LegionMember
	 */
	public boolean isLegionMember() {
		return legionMember != null;
	}

	/**
	 * @param legionMember
	 *          the legionMember to set
	 */
	public void setLegionMember(LegionMember legionMember) {
		this.legionMember = legionMember;
	}

	/**
	 * @return the legionMember
	 */
	public LegionMember getLegionMember() {
		return legionMember;
	}

	/**
	 * @return the legion
	 */
	public Legion getLegion() {
		return legionMember != null ? legionMember.getLegion() : null;
	}

	/**
	 * @return true if a player has a store opened
	 */
	public boolean hasStore() {
		if (getStore() != null)
			return true;
		return false;
	}

	/**
	 * Removes legion from player
	 */
	public void resetLegionMember() {
		setLegionMember(null);
	}

	public boolean isInGroup() {
		return playerGroup != null;
	}

	/**
	 * @return The account name of this player.
	 */
	public String getAccountName() {
		return playerAccount.getName();
	}

	public int getWarehouseExpansions() {
		return getCommonData().getWhNpcExpands() + getCommonData().getWhBonusExpands();
	}

	public int getWhNpcExpands() {
		return getCommonData().getWhNpcExpands();
	}

	public int getWhBonusExpands() {
		return getCommonData().getWhBonusExpands();
	}

	public void setWarehouseLimit() {
		getWarehouse().setLimit(StorageType.REGULAR_WAREHOUSE.getLimit() + (getWarehouseExpansions() * getWarehouse().getRowLength()));
	}

	/**
	 * @return regularWarehouse
	 */
	public Storage getWarehouse() {
		return regularWarehouse;
	}

	/**
	 * 0: regular, 1: fly, 2: glide its bitset
	 */
	public int getFlyState() {
		return flyState;
	}

	public void setFlyState(FlyState flyState) {
		this.flyState |= flyState.getId();
	}

	public void unsetFlyState(FlyState flyState) {
		this.flyState &= ~flyState.getId();
	}

	public boolean isInFlyState(FlyState flyState) {
		return (this.flyState & flyState.getId()) == flyState.getId();
	}

	/**
	 * CreatureState is unreliable for players returns true if player is flying or gliding
	 *
	 * @return boolean
	 */
	@Override
	public boolean isFlying() {
		return flyState >= 1;
	}

	/**
	 * CreatureState is unreliable for players returns true if player is flying
	 *
	 * @return boolean
	 */
	@Override
	public boolean isInFlyingState() {
		return isInFlyState(FlyState.FLYING);
	}

	public boolean isInGlidingState() {
		return isInFlyState(FlyState.GLIDING);
	}

	public boolean isTrading() {
		return ExchangeService.getInstance().isPlayerInExchange(this);
	}

	public boolean isInPrison() {
		return getPrisonDurationSeconds() > 0;
	}

	public void setPrisonEndTimeMillis(long prisonEndTimeMillis) {
		this.prisonEndTimeMillis = prisonEndTimeMillis;
	}

	public int getPrisonDurationSeconds() {
		if (prisonEndTimeMillis == 0)
			return 0;
		int durationSeconds = (int) ((prisonEndTimeMillis - System.currentTimeMillis()) / 1000);
		if (durationSeconds < 0)
			prisonEndTimeMillis = durationSeconds = 0;
		return durationSeconds;
	}

	public boolean isProtectionActive() {
		return isInVisualState(CreatureVisualState.BLINKING);
	}

	@Override
	public boolean isInvulnerable() {
		return isInCustomState(CustomPlayerState.INVULNERABLE);
	}

	public void setMailbox(Mailbox mailbox) {
		this.mailbox = mailbox;
	}

	public Mailbox getMailbox() {
		return mailbox;
	}

	/**
	 * @return the flyController
	 */
	public FlyController getFlyController() {
		return flyController;
	}

	/**
	 * @param flyController
	 *          the flyController to set
	 */
	public void setFlyController(FlyController flyController) {
		this.flyController = flyController;
	}

	/**
	 * @param craftingTask
	 */
	public void setCraftingTask(CraftingTask craftingTask) {
		this.craftingTask = craftingTask;
	}

	/**
	 * @return
	 */
	public CraftingTask getCraftingTask() {
		return craftingTask;
	}

	/**
	 * @param flightTeleportId
	 */
	public void setFlightTeleportId(int flightTeleportId) {
		this.flightTeleportId = flightTeleportId;
	}

	/**
	 * @return flightTeleportId
	 */
	public int getFlightTeleportId() {
		return flightTeleportId;
	}

	/**
	 * @param flightDistance
	 */
	public void setFlightDistance(int flightDistance) {
		this.flightDistance = flightDistance;

	}

	/**
	 * @param path
	 */
	public void setCurrentFlypath(FlyPathEntry path) {
		this.flyLocationId = path;
		if (path != null)
			this.flyStartTime = System.currentTimeMillis();
		else
			this.flyStartTime = 0;
	}

	/**
	 * @return flightDistance
	 */
	public int getFlightDistance() {
		return flightDistance;
	}

	/**
	 * @return
	 */
	public boolean isUsingFlyTeleport() {
		return isInState(CreatureState.FLYING) && flightTeleportId != 0;
	}

	/**
	 * @param accessLevel
	 * @return True if the player has the specified access level or higher
	 */
	public boolean hasAccess(byte accessLevel) {
		return playerAccount.getAccessLevel() >= accessLevel;
	}

	/**
	 * @return True if the player is a member of the server staff
	 */
	public boolean isStaff() {
		return playerAccount.getAccessLevel() > 0;
	}

	@Override
	public boolean isEnemy(Creature creature) {
		return creature.isEnemyFrom(this) || isEnemyFrom(creature);
	}

	@Override
	public boolean isEnemyFrom(Npc enemy) {
		switch (enemy.getType(this)) {
			case AGGRESSIVE:
			case ATTACKABLE:
				return true;
		}
		return false;
	}

	/**
	 * Player enemies:<br>
	 * - different race<br>
	 * - duel partner<br>
	 * - in pvp zone
	 *
	 * @param enemy
	 * @return
	 */
	@Override
	public boolean isEnemyFrom(Player enemy) {
		if (equals(enemy))
			return false;
		if (isInCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS) || enemy.isInCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS)) {
			return !isInFfaTeamMode() || !enemy.isInFfaTeamMode() || !isInSameTeam(enemy);
		}
		return canPvP(enemy) || isDueling(enemy);
	}

	public boolean isAggroIconTo(Player enemy) {
		if (isInCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS) || enemy.isInCustomState(CustomPlayerState.ENEMY_OF_ALL_PLAYERS)) {
			return !isInFfaTeamMode() || !enemy.isInFfaTeamMode() || !isInSameTeam(enemy);
		}
		return enemy.getRace() != getRace();
	}

	public void setInFfaTeamMode(boolean isInFfaTeamMode) {
		this.isInFfaTeamMode = isInFfaTeamMode;
	}

	public boolean isInFfaTeamMode() {
		return isInFfaTeamMode;
	}

	private boolean canPvP(Player enemy) {
		int worldId = enemy.getWorldId();
		if (!enemy.getRace().equals(getRace())) {
			return isInsidePvPZone() && enemy.isInsidePvPZone();
		} else if (worldId == 110010000 || worldId == 120010000 || isInInstance()) {
			return isInsideZoneType(ZoneType.PVP) && enemy.isInsideZoneType(ZoneType.PVP) && !isInSameTeam(enemy);
		}
		return false;
	}

	public boolean isDueling(Player player) {
		return DuelService.getInstance().isDueling(player, this);
	}

	public boolean isInSameTeam(Player player) {
		int teamId = getCurrentTeamId();
		return teamId != 0 && teamId == player.getCurrentTeamId();
	}

	@Override
	public boolean canSee(VisibleObject object) {
		if (object instanceof Pet && !equals(((Pet) object).getMaster()) && !getKnownList().sees(((Pet) object).getMaster()))
			return false; // pet spawn packet must be sent after owner's

		if (super.canSee(object))
			return true;

		if (object instanceof Creature) {
			if (((Creature) object).getMaster() instanceof Player) { // player or a summon's master
				Player player = (Player) ((Creature) object).getMaster();
				if (isInSameTeam(player) && !isDueling(player))
					return true;
			}
			if (object instanceof Kisk && ((Kisk) object).getOwnerRace() == getRace()) // invisible kisks can be seen from players of the same race
				return true;
		}

		return false;
	}

	@Override
	public TribeClass getTribe() {
		TribeClass transformTribe = getTransformModel().getTribe();
		if (transformTribe != null) {
			return transformTribe;
		}
		return getRace() == Race.ELYOS ? TribeClass.PC : TribeClass.PC_DARK;
	}

	@Override
	public TribeClass getBaseTribe() {
		TribeClass transformTribe = getTransformModel().getTribe();
		if (transformTribe != null) {
			return DataManager.TRIBE_RELATIONS_DATA.getBaseTribe(transformTribe);
		}
		return getTribe();
	}

	/**
	 * @return the summon
	 */
	public Summon getSummon() {
		return summon;
	}

	/**
	 * @param summon
	 *          the summon to set
	 */
	public void setSummon(Summon summon) {
		this.summon = summon;
	}

	/**
	 * @param newKisk
	 *          kisk to bind to (null if unbinding)
	 */
	public void setKisk(Kisk newKisk) {
		this.kisk = newKisk;
	}

	public Kisk getKisk() {
		return kisk;
	}

	public boolean hasCooldown(Item item) {
		ItemUseLimits limits = item.getItemTemplate().getUseLimits();
		if (limits == null)
			return false;

		long reuseTime = getItemReuseTime(limits.getDelayId());
		if (reuseTime == 0)
			return false;

		if (reuseTime <= System.currentTimeMillis()) {
			itemCoolDowns.remove(limits.getDelayId());
			return false;
		}
		return true;
	}

	public long getItemReuseTime(int delayId) {
		ItemCooldown cd = itemCoolDowns.get(delayId);
		return cd == null ? 0 : cd.getReuseTime();
	}

	public Map<Integer, ItemCooldown> getItemCoolDowns() {
		return itemCoolDowns;
	}

	/**
	 * @param delayId
	 * @param time
	 * @param useDelay
	 */
	public void addItemCoolDown(int delayId, long time, int useDelay) {
		itemCoolDowns.put(delayId, new ItemCooldown(time, useDelay));
	}

	public void removeItemCoolDown(int delayId) {
		itemCoolDowns.remove(delayId);
	}

	public void setPlayerResActivate(boolean isActivated) {
		this.isResByPlayer = isActivated;
	}

	public boolean getResStatus() {
		return isResByPlayer;
	}

	public int getResurrectionSkill() {
		return resurrectionSkill;
	}

	public void setResurrectionSkill(int resurrectionSkill) {
		this.resurrectionSkill = resurrectionSkill;
	}

	public void setIsFlyingBeforeDeath(boolean isActivated) {
		this.isFlyingBeforeDeath = isActivated;
	}

	public boolean getIsFlyingBeforeDeath() {
		return isFlyingBeforeDeath;
	}

	public PlayerAlliance getPlayerAlliance() {
		return playerAllianceGroup != null ? playerAllianceGroup.getAlliance() : null;
	}

	public PlayerAllianceGroup getPlayerAllianceGroup() {
		return playerAllianceGroup;
	}

	public boolean isInAlliance() {
		return playerAllianceGroup != null;
	}

	public void setPlayerAllianceGroup(PlayerAllianceGroup playerAllianceGroup) {
		this.playerAllianceGroup = playerAllianceGroup;
	}

	public final boolean isInLeague() {
		return isInAlliance() && getPlayerAlliance().isInLeague();
	}

	public final boolean isInTeam() {
		return isInGroup() || isInAlliance();
	}

	/**
	 * @return current {@link PlayerGroup}, {@link PlayerAlliance} or null
	 */
	public final TemporaryPlayerTeam<? extends TeamMember<Player>> getCurrentTeam() {
		return isInGroup() ? getPlayerGroup() : getPlayerAlliance();
	}

	/**
	 * @return current {@link PlayerGroup}, {@link PlayerAllianceGroup} or null
	 */
	public final TemporaryPlayerTeam<? extends TeamMember<Player>> getCurrentGroup() {
		return isInGroup() ? getPlayerGroup() : getPlayerAllianceGroup();
	}

	/**
	 * @return current team id, 0 if not in a team
	 */
	public final int getCurrentTeamId() {
		TemporaryPlayerTeam<? extends TeamMember<Player>> team = getCurrentTeam();
		return team == null ? 0 : team.getTeamId();
	}

	public PortalCooldownList getPortalCooldownList() {
		return portalCooldownList;
	}

	public Cooldowns getCraftCooldowns() {
		return craftCooldowns;
	}

	public Cooldowns getHouseObjectCooldowns() {
		return houseObjectCooldowns;
	}

	public Npc getPostman() {
		return postman;
	}

	public void setPostman(Npc postman) {
		this.postman = postman;
	}

	public PlayerAccountData getAccountData() {
		return playerAccountData;
	}

	public Account getAccount() {
		return playerAccount;
	}

	public Timestamp getCreationDate() {
		return playerAccountData.getCreationDate();
	}

	/**
	 * Quest completion
	 *
	 * @param questId
	 * @return
	 */
	public boolean isCompleteQuest(int questId) {
		QuestState qs = getQuestStateList().getQuestState(questId);
		return qs != null && qs.getStatus() == QuestStatus.COMPLETE;
	}

	public long getNextSkillUse() {
		return nextSkillUse;
	}

	public void setNextSkillUse(long nextSkillUse) {
		this.nextSkillUse = nextSkillUse;
	}

	/**
	 * chain skills
	 */
	public ChainSkills getChainSkills() {
		if (chainSkills == null)
			chainSkills = new ChainSkills();
		return chainSkills;
	}

	public void setLastCounterSkill(AttackStatus status) {
		AttackStatus result = AttackStatus.getBaseStatus(status);

		switch (result) {
			case DODGE:
			case PARRY:
			case BLOCK:
			case RESIST:
				lastCounterSkill.put(result, System.currentTimeMillis());
				break;
		}
	}

	public long getLastCounterSkill(AttackStatus status) {
		if (lastCounterSkill.get(status) == null)
			return 0;

		return lastCounterSkill.get(status);
	}

	/**
	 * @return the Resurrection Positional State
	 */
	public boolean isInResPostState() {
		return isInResurrectPosState;
	}

	/**
	 * @param the
	 *          Resurrection Positional State to set
	 */
	public void setResPosState(boolean value) {
		this.isInResurrectPosState = value;
	}

	/**
	 * @param the
	 *          Resurrection Positional X value to set
	 */
	public void setResPosX(float value) {
		this.resPosX = value;
	}

	/**
	 * @return the Resurrection Positional X value
	 */
	public float getResPosX() {
		return resPosX;
	}

	/**
	 * @param the
	 *          Resurrection Positional Y value to set
	 */
	public void setResPosY(float value) {
		this.resPosY = value;
	}

	/**
	 * @return the Resurrection Positional Y value
	 */
	public float getResPosY() {
		return resPosY;
	}

	/**
	 * @param the
	 *          Resurrection Positional Z value to set
	 */
	public void setResPosZ(float value) {
		this.resPosZ = value;
	}

	/**
	 * @return the Resurrection Positional Z value
	 */
	public float getResPosZ() {
		return resPosZ;
	}

	public boolean isInSiegeWorld() {
		switch (getWorldId()) {
			case 210050000:
			case 220070000:
			case 400010000:
				return true;
			default:
				return false;
		}
	}

	public boolean hasPermission(byte perm) {
		return playerAccount.getMembership() >= perm;
	}

	/**
	 * @return Returns the emotions.
	 */
	public EmotionList getEmotions() {
		return emotions;
	}

	/**
	 * @param emotions
	 *          The emotions to set.
	 */
	public void setEmotions(EmotionList emotions) {
		this.emotions = emotions;
	}

	public BindPointPosition getBindPoint() {
		return bindPoint;
	}

	public void setBindPoint(BindPointPosition bindPoint) {
		this.bindPoint = bindPoint;
	}

	public int speedHackCounter;
	public int abnormalHackCounter;

	@Override
	public ItemAttackType getAttackType() {
		Item weapon = getEquipment().getMainHandWeapon();
		if (weapon != null)
			return weapon.getItemTemplate().getAttackType();
		return ItemAttackType.PHYSICAL;
	}

	public long getFlyStartTime() {
		return flyStartTime;
	}

	public FlyPathEntry getCurrentFlyPath() {
		return flyLocationId;
	}

	public void resetAbyssRankListUpdated() {
		this.abyssRankListUpdateMask = 0;
	}

	public void setAbyssRankListUpdated(AbyssRankUpdateType type) {
		this.abyssRankListUpdateMask |= type.value();
	}

	public boolean isAbyssRankListUpdated(AbyssRankUpdateType type) {
		return (abyssRankListUpdateMask & type.value()) == type.value();
	}

	public void addSalvationPoints(long points) {
		getCommonData().addSalvationPoints(points);
		PacketSendUtility.sendPacket(this, new SM_STATS_INFO(this));
	}

	@Override
	public boolean isPvpTarget(Creature creature) {
		return creature.getActingCreature() instanceof Player;
	}

	public boolean isTargetingNpcWithFunction(int objectId, int dialogActionId) {
		VisibleObject target = getTarget();
		return target instanceof Npc && target.getObjectId() == objectId && ((Npc) target).getObjectTemplate().supportsAction(dialogActionId);
	}

	/**
	 * @return the motions
	 */
	public MotionList getMotions() {
		return motions;
	}

	/**
	 * @param motions
	 *          the motions to set
	 */
	public void setMotions(MotionList motions) {
		this.motions = motions;
	}

	/**
	 * @return the npcFactions
	 */
	public NpcFactions getNpcFactions() {
		return npcFactions;
	}

	/**
	 * @param npcFactions
	 *          the npcFactions to set
	 */
	public void setNpcFactions(NpcFactions npcFactions) {
		this.npcFactions = npcFactions;
	}

	/**
	 * @return the flyReuseTime
	 */
	public long getFlyReuseTime() {
		return flyReuseTime;
	}

	/**
	 * @param flyReuseTime
	 *          the flyReuseTime to set
	 */
	public void setFlyReuseTime(long flyReuseTime) {
		this.flyReuseTime = flyReuseTime;
	}

	/**
	 * Stone Use Order determined by highest inventory slot. :( If player has two types, wrong one might be used.
	 *
	 * @param player
	 * @return selfRezItem
	 */
	public Item getSelfRezStone() {
		Item item;
		item = getReviveStone(161001001);
		if (item == null)
			item = getReviveStone(161000003);
		if (item == null)
			item = getReviveStone(161000004);
		if (item == null)
			item = getReviveStone(161000001);
		return item;
	}

	/**
	 * @param stoneItemId
	 * @return stoneItem or null
	 */
	private Item getReviveStone(int stoneId) {
		Item item = getInventory().getFirstItemByItemId(stoneId);
		if (item != null && hasCooldown(item))
			item = null;
		return item;
	}

	/**
	 * Need to find how an item is determined as able to self-rez.
	 *
	 * @return boolean can self rez with item
	 */
	public boolean haveSelfRezItem() {
		return (getSelfRezStone() != null);
	}

	public void unsetResPosState() {
		if (isInResPostState()) {
			setResPosState(false);
			setResPosX(0);
			setResPosY(0);
			setResPosZ(0);
		}
	}

	public LootGroupRules getLootGroupRules() {
		if (isInGroup()) {
			return getPlayerGroup().getLootGroupRules();
		}
		if (isInLeague()) {
			return getPlayerAlliance().getLeague().getLootGroupRules();
		}
		if (isInAlliance()) {
			return getPlayerAlliance().getLootGroupRules();
		}
		return null;
	}

	public boolean isLooting() {
		return lootingNpcOid != 0;
	}

	public void setLootingNpcOid(int lootingNpcOid) {
		this.lootingNpcOid = lootingNpcOid;
	}

	public int getLootingNpcOid() {
		return lootingNpcOid;
	}

	public final boolean isMentor() {
		return isMentor;
	}

	public final void setMentor(boolean isMentor) {
		this.isMentor = isMentor;
	}

	@Override
	public Race getRace() {
		return getCommonData().getRace();
	}

	public Race getOppositeRace() {
		return getRace() == Race.ELYOS ? Race.ASMODIANS : Race.ELYOS;
	}

	private Map<String, Object> vars = new HashMap<>();

	public boolean hasVar(String key) {
		return vars.containsKey(key);
	}

	public void setVar(String key, Object value, boolean persist) {
		vars.put(key, value);
		if (persist)
			PlayerVarsDAO.set(getObjectId(), key, value);
	}

	public Object getVar(String key) {
		return vars.get(key);
	}

	public int getVarInt(String key) {
		Object o = vars.get(key);
		if (o != null)
			return Integer.parseInt(o.toString());
		return 0;
	}

	public String getVarStr(String key) {
		Object o = vars.get(key);
		if (o != null)
			return o.toString();
		return null;
	}

	public void setVars(Map<String, Object> map) {
		this.vars = map;
	}

	@Override
	public int getSkillCooldown(SkillTemplate template) {
		return isInCustomState(CustomPlayerState.NO_SKILL_COOLDOWN_MODE) ? 0 : template.getCooldown();
	}

	public void setLastMessageTime() {
		if ((System.currentTimeMillis() - lastMsgTime) / 1000 < SecurityConfig.FLOOD_DELAY)
			floodMsgCount++;
		else
			floodMsgCount = 0;
		lastMsgTime = System.currentTimeMillis();
	}

	public int floodMsgCount() {
		return floodMsgCount;
	}

	public void setRebirthEffect(RebirthEffect rebirthEffect) {
		this.rebirthEffect = rebirthEffect;
	}

	public RebirthEffect getRebirthEffect() {
		return rebirthEffect;
	}

	public boolean canUseRebirthRevive() {
		return rebirthEffect != null || hasAccess(AdminConfig.AUTO_RES);
	}

	/**
	 * Put up supplements to subtraction queue, so that when moving they would not decrease, need update as confirmation To update use
	 * updateSupplements()
	 */
	public void subtractSupplements(int count, int supplementId) {
		subtractedSupplementsCount = count;
		subtractedSupplementId = supplementId;
	}

	/**
	 * Update supplements in queue and clear the queue
	 */
	public void updateSupplements() {
		if (subtractedSupplementId == 0 || subtractedSupplementsCount == 0)
			return;
		getInventory().decreaseByItemId(subtractedSupplementId, subtractedSupplementsCount);
		subtractedSupplementsCount = 0;
		subtractedSupplementId = 0;
	}

	public byte getPortAnimationId() {
		return portAnimation;
	}

	public void setPortAnimation(ArrivalAnimation portAnimation) {
		this.portAnimation = portAnimation.getId();
	}

	@Override
	public boolean isSkillDisabled(SkillTemplate template) {
		ChainCondition cond = template.getChainCondition();
		if (cond != null && cond.getAllowedActivations() > 1) { // exception for multicast
			int chainCount = getChainSkills().getCurrentChainCount(cond.getCategory());
			if (chainCount > 0 && chainCount < cond.getAllowedActivations() && !getChainSkills().isChainExpired())
				return false;
		}
		if (super.isSkillDisabled(template)) {
			PacketSendUtility.sendPacket(this, SM_SYSTEM_MESSAGE.STR_SKILL_NOT_READY());
			return true;
		}
		return false;
	}

	public List<House> getHouses() {
		if (houses == null)
			resetHouses();
		return houses;
	}

	public void resetHouses() {
		houses = HousingService.getInstance().findPlayerHouses(getObjectId());
	}

	public House getActiveHouse() {
		for (House house : getHouses())
			if (!house.isInactive())
				return house;

		return null;
	}

	public float[] getBattleReturnCoords() {
		return battleReturnCoords;
	}

	public void setBattleReturnCoords(int mapId, float[] coords) {
		this.battleReturnMap = mapId;
		this.battleReturnCoords = coords;
	}

	public int getBattleReturnMap() {
		return battleReturnMap;
	}

	public boolean isInSprintMode() {
		return isInSprintMode;
	}

	public void setSprintMode(boolean isInSprintMode) {
		this.isInSprintMode = isInSprintMode;
	}

	public void setRideObservers(ActionObserver observer) {
		if (rideObservers == null)
			rideObservers = new ArrayList<>();

		synchronized (rideObservers) {
			rideObservers.add(observer);
		}
	}

	public List<ActionObserver> getRideObservers() {
		return rideObservers;
	}

	public AbsoluteStatOwner getAbsoluteStats() {
		return absStatsHolder;
	}

	@Override
	public WorldPosition getPosition() {
		return getCommonData().getPosition();
	}

	@Override
	public void setPosition(WorldPosition position) {
		getMoveController().resetLastPositionFromClient(); // if we don't reset it, material collision handlers (such as shields) affect you on teleport
		getCommonData().setPosition(position);
	}

	public int getRobotId() {
		return robotId;
	}

	public void setRobotId(int robotId) {
		this.robotId = robotId;
	}

	public boolean isInRobotMode() {
		return robotId != 0;
	}

	@Override
	public boolean canPerformMove() {
		// player cannot move is transformed
		if (getTransformModel().getBanMovement() == 1)
			return false;

		return super.canPerformMove();
	}

	@Override
	public String toString() {
		return "Player [id=" + getObjectId() + ", name=" + getName() + "]";
	}

	public void setCustomState(CustomPlayerState state) {
		customStates |= state.getMask();
	}

	public void unsetCustomState(CustomPlayerState state) {
		customStates &= ~state.getMask();
	}

	public boolean isInCustomState(CustomPlayerState state) {
		return (customStates & state.getMask()) == state.getMask();
	}

	public void incrementFearCountAndUpdateExpirationTime(long duration) {
		fearCount.incrementAndGet();
		cumulativeFearResistExpirationTime.set(System.currentTimeMillis() + duration + 1000); // +1s to compensate for hittime and differences between retail
	}

	public void incrementSleepCountAndUpdateExpirationTime(long duration) {
		sleepCount.incrementAndGet();
		cumulativeSleepResistExpirationTime.set(System.currentTimeMillis() + duration + 1000); // +1s to compensate for hittime and differences between retail
	}

	public void incrementParalyzeCountAndUpdateExpirationTime(long duration) {
		paralyzeCount.incrementAndGet();
		cumulativeParalyzeResistExpirationTime.set(System.currentTimeMillis() + duration + 1000); // +1s to compensate for hittime and differences between retail
	}

	public int getFearCount() {
		return fearCount.get();
	}

	public int getSleepCount() {
		return sleepCount.get();
	}

	public int getParalyzeCount() {
		return paralyzeCount.get();
	}

	public void resetFearCount() {
		fearCount.set(0);
		cumulativeFearResistExpirationTime.set(0);
	}

	public void resetSleepCount() {
		sleepCount.set(0);
		cumulativeSleepResistExpirationTime.set(0);
	}

	public void resetParalyzeCount() {
		paralyzeCount.set(0);
		cumulativeParalyzeResistExpirationTime.set(0);
	}

	public boolean validateCumulativeFearResistExpirationTime() {
		if (System.currentTimeMillis() > cumulativeFearResistExpirationTime.get()) {
			return true;
		} else {
			resetFearCount();
			return false;
		}
	}

	public boolean validateCumulativeSleepResistExpirationTime() {
		if (System.currentTimeMillis() > cumulativeSleepResistExpirationTime.get()) {
			return true;
		} else {
			resetSleepCount();
			return false;
		}
	}

	public boolean validateCumulativeParalyzeResistExpirationTime() {
		if (System.currentTimeMillis() > cumulativeParalyzeResistExpirationTime.get()) {
			return true;
		} else {
			resetParalyzeCount();
			return false;
		}
	}
}
