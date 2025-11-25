package com.aionemu.gameserver.services.player;

import java.sql.Timestamp;
import java.util.List;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.configs.main.MembershipConfig;
import com.aionemu.gameserver.configs.main.NameConfig;
import com.aionemu.gameserver.controllers.FlyController;
import com.aionemu.gameserver.controllers.effect.PlayerEffectController;
import com.aionemu.gameserver.dao.*;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerInitialData;
import com.aionemu.gameserver.dataholders.PlayerInitialData.LocationData;
import com.aionemu.gameserver.dataholders.PlayerInitialData.PlayerCreationData;
import com.aionemu.gameserver.dataholders.PlayerInitialData.PlayerCreationData.ItemType;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Mailbox;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.aionemu.gameserver.model.stats.calc.functions.PlayerStatFunctions;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.EmotionLearnAction;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.PunishmentService.PunishmentType;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.KnownList;

/**
 * This class is designed to do all the work related with loading/storing players.<br>
 * Same with storing, {@link #storePlayer(com.aionemu.gameserver.model.gameobjects.player.Player)} stores all player data like appearance, items,
 * etc...
 *
 * @author SoulKeeper, Saelya, Cura
 */
public class PlayerService {

	/**
	 * @return True if the character name is taken (currently used by another character or the character with this name was recently renamed).
	 */
	public static boolean isNameUsedOrReserved(String oldName, String newName) {
		return isNameUsedOrReserved(oldName, newName, NameConfig.RESERVE_OLD_NAME_DAYS);
	}

	public static boolean isNameUsedOrReserved(String oldName, String newName, int nameReservationDurationDays) {
		return PlayerDAO.isNameUsed(newName) || OldNamesDAO.isNameReserved(oldName, newName, nameReservationDurationDays);
	}

	/**
	 * Stores newly created player
	 *
	 * @param player
	 *          player to store
	 * @return true if character was successful saved.
	 */
	public static boolean storeNewPlayer(Player player, String accountName, int accountId) {
		return PlayerDAO.saveNewPlayer(player, accountId, accountName)
			&& PlayerAppearanceDAO.store(player) && PlayerSkillListDAO.storeSkills(player)
			&& InventoryDAO.store(player);
	}

	/**
	 * Stores player data into db
	 *
	 * @param player
	 */
	public static void storePlayer(Player player) {
		PlayerDAO.storePlayer(player);
		PlayerSkillListDAO.storeSkills(player);
		PlayerSettingsDAO.saveSettings(player);
		PlayerQuestListDAO.store(player);
		AbyssRankDAO.storeAbyssRank(player);
		PlayerPunishmentsDAO.storePlayerPunishment(player, PunishmentType.PRISON);
		PlayerPunishmentsDAO.storePlayerPunishment(player, PunishmentType.GATHER);
		InventoryDAO.store(player);
		for (House house : player.getHouses())
			house.save();
		ItemStoneListDAO.save(player);
		MailDAO.storeMailbox(player);
		PortalCooldownsDAO.storePortalCooldowns(player);
		CraftCooldownsDAO.storeCraftCooldowns(player);
		HouseObjectCooldownsDAO.storeHouseObjectCooldowns(player);
		PlayerNpcFactionsDAO.storeNpcFactions(player);
		AccountPassportsDAO.storePassport(player.getAccount());
		if (EventsConfig.ENABLE_HEADHUNTING)
			HeadhuntingDAO.storeHeadhunter(player.getObjectId());
	}

	public static Player getPlayer(int playerObjId, Account account) {
		// Player common data and appearance should be already loaded in account
		PlayerAccountData playerAccountData = account.getPlayerAccountData(playerObjId);
		PlayerCommonData pcd = playerAccountData.getPlayerCommonData();
		Player player = new Player(playerAccountData, account);
		int oldOwnerId = pcd.getWorldOwnerId();
		player.setPosition(World.getInstance().createPosition(pcd.getMapId(), pcd.getX(), pcd.getY(), pcd.getZ(), pcd.getHeading(), 0));
		pcd.setWorldOwnerId(oldOwnerId);
		LegionMember legionMember = LegionService.getInstance().getLegionMember(pcd);
		if (legionMember != null) {
			player.setLegionMember(legionMember);
		}

		player.setMacros(PlayerMacrosDAO.loadMacros(playerObjId));
		player.setSkillList(PlayerSkillListDAO.loadSkillList(playerObjId));
		player.setKnownlist(new KnownList(player));
		player.setFriendList(FriendListDAO.load(player));
		player.setBlockList(BlockListDAO.load(playerObjId));
		player.setTitleList(PlayerTitleListDAO.loadTitleList(playerObjId));
		player.setPlayerSettings(PlayerSettingsDAO.loadSettings(playerObjId));
		AbyssRankDAO.loadAbyssRank(player);
		PlayerNpcFactionsDAO.loadNpcFactions(player);
		MotionDAO.loadMotionList(player);
		AccountPassportsDAO.loadPassport(player.getAccount());
		player.setEffectController(new PlayerEffectController(player));
		player.setFlyController(new FlyController(player));
		PlayerStatFunctions.addPredefinedStatFunctions(player);

		player.setQuestStateList(PlayerQuestListDAO.load(playerObjId));
		player.setRecipeList(PlayerRecipesDAO.load(player.getObjectId()));

		account.getAccountWarehouse().setOwner(player);
		InventoryDAO.loadStorage(playerObjId, player.getInventory());
		ItemStoneListDAO.load(player.getInventory().getItems());
		ItemStoneListDAO.load(player.getEquipment().getEquippedItemsWithoutStigma());

		InventoryDAO.loadStorage(playerObjId, player.getWarehouse());
		ItemStoneListDAO.load(player.getWarehouse().getItems());

		for (Storage petBag : player.getPetBags()) {
			InventoryDAO.loadStorage(playerObjId, petBag);
			ItemStoneListDAO.load(petBag.getItems());
		}
		for (Storage cabinet : player.getCabinets()) {
			InventoryDAO.loadStorage(playerObjId, cabinet);
			ItemStoneListDAO.load(cabinet.getItems());
		}

		// Apply equipment stats (items and manastones were loaded in account)
		player.getEquipment().onLoadApplyEquipmentStats();

		PlayerPunishmentsDAO.loadPlayerPunishments(player);

		// load saved effects
		PlayerEffectsDAO.loadPlayerEffects(player);
		// load saved player cooldowns
		PlayerCooldownsDAO.loadPlayerCooldowns(player);
		// load item cooldowns
		ItemCooldownsDAO.loadItemCooldowns(player);
		// load portal cooldowns
		PortalCooldownsDAO.loadPortalCooldowns(player);
		// load house object use cooldowns
		HouseObjectCooldownsDAO.loadHouseObjectCooldowns(player);
		// load bind point
		PlayerBindPointDAO.loadBindPoint(player);
		// load craft cooldowns
		CraftCooldownsDAO.loadCraftCooldowns(player);

		PlayerLifeStatsDAO.loadPlayerLifeStat(player);
		PlayerEmotionListDAO.loadEmotions(player);
		if (player.hasPermission(MembershipConfig.EMOTIONS_ALL)) {
			for (int emotionId : EmotionLearnAction.getLearnableEmotionIds())
				player.getEmotions().add(emotionId, 0, false);
		}

		return player;
	}

	/**
	 * This method is used for creating new players
	 *
	 * @param playerAccountData
	 * @param account
	 * @return Player
	 */
	public static Player newPlayer(PlayerAccountData playerAccountData, Account account) {
		PlayerCommonData playerCommonData = playerAccountData.getPlayerCommonData();
		PlayerInitialData playerInitialData = DataManager.PLAYER_INITIAL_DATA;
		LocationData ld = playerInitialData.getSpawnLocation(playerCommonData.getRace());

		playerCommonData.setMapId(ld.getMapId());
		playerCommonData.setX(ld.getX());
		playerCommonData.setY(ld.getY());
		playerCommonData.setZ(ld.getZ());
		playerCommonData.setHeading(ld.getHeading());

		Player newPlayer = new Player(playerAccountData, account);

		// Starting skills
		newPlayer.setSkillList(new PlayerSkillList());
		SkillLearnService.learnNewSkills(newPlayer, 1, newPlayer.getLevel());

		// Starting items
		PlayerCreationData playerCreationData = playerInitialData.getPlayerCreationData(playerCommonData.getPlayerClass());
		if (playerCreationData != null) { // player transfer
			List<ItemType> items = playerCreationData.getItems();
			for (ItemType itemType : items) {
				int itemId = itemType.getTemplate().getTemplateId();
				Item item = ItemFactory.newItem(itemId, itemType.getCount());
				if (item == null) {
					continue;
				}

				// When creating new player - all equipment that has slot values will be equipped
				// Make sure you will not put into xml file more items than possible to equip.
				ItemTemplate itemTemplate = item.getItemTemplate();

				if ((itemTemplate.isArmor() || itemTemplate.isWeapon()) && !newPlayer.getEquipment().isSlotEquipped(itemTemplate.getItemSlot())) {
					item.setEquipped(true);
					ItemSlot itemSlot = ItemSlot.getSlotFor(itemTemplate.getItemSlot());
					item.setEquipmentSlot(itemSlot.getSlotIdMask());
				}
				newPlayer.getInventory().onLoadHandler(item);
			}
		}
		newPlayer.setMailbox(new Mailbox(newPlayer));

		// Mark inventory and equipment as UPDATE_REQUIRED to be saved during character creation
		newPlayer.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
		newPlayer.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
		return newPlayer;
	}

	public static PlayerCommonData getOrLoadPlayerCommonData(int playerObjId) {
		Player player = World.getInstance().getPlayer(playerObjId);
		if (player == null)
			return PlayerDAO.loadPlayerCommonData(playerObjId);
		return player.getCommonData();
	}

	public static PlayerCommonData getOrLoadPlayerCommonData(String name) {
		Player player = World.getInstance().getPlayer(name);
		if (player == null)
			return PlayerDAO.loadPlayerCommonDataByName(name);
		return player.getCommonData();
	}

	/**
	 * Cancel Player deletion process if its possible.
	 *
	 * @param accData
	 *          PlayerAccountData
	 * @return True if deletion was successful canceled.
	 */
	public static boolean cancelPlayerDeletion(PlayerAccountData accData) {
		if (accData.getDeletionDate() == null) {
			return true;
		}

		if (accData.getDeletionDate().getTime() > System.currentTimeMillis()) {
			accData.setDeletionDate(null);
			storeDeletionTime(accData);
			return true;
		}
		return false;
	}

	/**
	 * Starts player deletion process if its possible. If deletion is possible character should be deleted after 5 minutes.
	 *
	 * @param accData
	 *          PlayerAccountData
	 */
	public static void deletePlayer(PlayerAccountData accData) {
		if (accData.getDeletionDate() != null) {
			return;
		}

		accData.setDeletionDate(new Timestamp(System.currentTimeMillis() + CustomConfig.CHARACTER_DELETION_TIME_MINUTES * 60 * 1000));
		storeDeletionTime(accData);
	}

	/**
	 * Completely removes player from database
	 *
	 * @param playerId
	 *          id of player to delete from db
	 */
	public static void deletePlayerFromDB(int playerId) {
		deletePlayerFromDB(playerId, true);
	}

	public static void deletePlayerFromDB(int playerId, boolean notifyServices) {
		InventoryDAO.deletePlayerOrLegionItems(playerId);
		PlayerDAO.deletePlayer(playerId);
		if (notifyServices) {
			HousingService.getInstance().onPlayerDeleted(playerId);
			BrokerService.getInstance().onPlayerDeleted(playerId);
		}
	}

	/**
	 * Updates deletion time in database
	 *
	 * @param accData
	 *          PlayerAccountData
	 */
	private static void storeDeletionTime(PlayerAccountData accData) {
		PlayerDAO.updateDeletionTime(accData.getPlayerCommonData().getPlayerObjId(), accData.getDeletionDate());
	}

	/**
	 * @param objectId
	 * @param creationDate
	 */
	public static void storeCreationTime(int objectId, Timestamp creationDate) {
		PlayerDAO.storeCreationTime(objectId, creationDate);
	}

	/**
	 * Add macro for player
	 *
	 * @param player
	 *          Player
	 * @param macroOrder
	 *          Macro order
	 * @param macroXML
	 *          Macro XML
	 */
	public static void addMacro(Player player, int macroOrder, String macroXML) {
		if (player.getMacros().add(macroOrder, macroXML)) {
			PlayerMacrosDAO.addMacro(player.getObjectId(), macroOrder, macroXML);
		} else {
			PlayerMacrosDAO.updateMacro(player.getObjectId(), macroOrder, macroXML);
		}
	}

	/**
	 * Remove macro with specified index from specified player
	 *
	 * @param player
	 *          Player
	 * @param macroOrder
	 *          Macro order index
	 */
	public static void removeMacro(Player player, int macroOrder) {
		if (player.getMacros().remove(macroOrder)) {
			PlayerMacrosDAO.deleteMacro(player.getObjectId(), macroOrder);
		}
	}

	public static String getPlayerName(int objectId) {
		Player player = World.getInstance().getPlayer(objectId);
		if (player != null)
			return player.getName();
		return PlayerDAO.getPlayerNameByObjId(objectId);
	}
}
