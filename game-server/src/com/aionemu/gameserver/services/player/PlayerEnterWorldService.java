package com.aionemu.gameserver.services.player;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.cache.HTMLCache;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.*;
import com.aionemu.gameserver.custom.pvpmap.PvpMapService;
import com.aionemu.gameserver.dao.*;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerInitialData;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Expirable;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.CharacterBanInfo;
import com.aionemu.gameserver.model.account.CharacterPasskey.ConnectType;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.BindPointPosition;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.items.storage.IStorage;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.model.templates.housing.HouseAddress;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ENTER_WORLD_CHECK.Msg;
import com.aionemu.gameserver.network.aion.skillinfo.SkillEntryWriter;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.services.*;
import com.aionemu.gameserver.services.PunishmentService.PunishmentType;
import com.aionemu.gameserver.services.abyss.AbyssSkillService;
import com.aionemu.gameserver.services.craft.RelinquishCraftStatus;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.mail.MailService;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.reward.VeteranRewardService;
import com.aionemu.gameserver.services.teleport.BindPointTeleportService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.services.toypet.PetService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.GMService;
import com.aionemu.gameserver.utils.collections.DynamicServerPacketBodySplitList;
import com.aionemu.gameserver.utils.collections.FixedElementCountSplitList;
import com.aionemu.gameserver.utils.collections.SplitList;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer, Neon
 */
public final class PlayerEnterWorldService {

	private static final Logger log = LoggerFactory.getLogger("GAMECONNECTION_LOG");
	private static final String VERSION_INFO = "Server " + GameServer.versionInfo.getBuildInfo(GSConfig.TIME_ZONE_ID);
	private static final ConcurrentLinkedQueue<Integer> enteringWorld = new ConcurrentLinkedQueue<>();

	public static void enterWorld(final AionConnection client, int objectId) {
		Account account = client.getAccount();
		PlayerAccountData playerAccData = account.getPlayerAccountData(objectId);
		if (playerAccData == null) {
			log.warn("Player enterWorld fail: character obj ID {} was not found on account ID {}.", objectId, account.getId());
			client.sendPacket(new SM_ENTER_WORLD_CHECK(Msg.CONNECTION_ERROR));
			return;
		}

		PlayerCommonData pcd = playerAccData.getPlayerCommonData();
		if (pcd == null) {
			log.warn("Player enterWorld fail: CommonData for character obj ID {} is null.", objectId);
			client.sendPacket(new SM_ENTER_WORLD_CHECK(Msg.CONNECTION_ERROR));
			return;
		}

		if (pcd.isOnline()) { // character should soon leave the world (due to previous client crash)
			if (DAOManager.getDAO(PlayerDAO.class).isOnline(objectId)) {
				client.sendPacket(new SM_ENTER_WORLD_CHECK(Msg.REENTRY_TIME));
				return;
			} else { // reload pcd, appearance, acc warehouse, ... since char was saved after acc logged in (delayed kick)
				playerAccData = AccountService.loadPlayerAccountData(objectId);
				pcd = playerAccData.getPlayerCommonData();
				account.addPlayerAccountData(playerAccData);
				account.setAccountWarehouse(AccountService.loadAccountWarehouse(account));
			}
		}

		if (World.getInstance().isInWorld(objectId)) {
			log.warn("Player enterWorld fail: Duplicate character obj ID {} found in world.", objectId);
			client.sendPacket(new SM_ENTER_WORLD_CHECK(Msg.CONNECTION_ERROR));
			return;
		}

		// check if char is banned
		CharacterBanInfo cbi = playerAccData.getCharBanInfo();
		if (cbi != null) {
			if (cbi.getEnd() >= System.currentTimeMillis() / 1000) {
				client.sendPacket(new SM_ENTER_WORLD_CHECK(Msg.CONNECTION_ERROR));
				return;
			} else {
				DAOManager.getDAO(PlayerPunishmentsDAO.class).unpunishPlayer(objectId, PunishmentType.CHARBAN);
			}
		}

		// passkey check
		if (SecurityConfig.PASSKEY_ENABLE && !account.getCharacterPasskey().isPass()) {
			account.getCharacterPasskey().setConnectType(ConnectType.ENTER);
			account.getCharacterPasskey().setObjectId(objectId);
			boolean isExistPasskey = DAOManager.getDAO(PlayerPasskeyDAO.class).existCheckPlayerPasskey(account.getId());
			client.sendPacket(new SM_CHARACTER_SELECT(!isExistPasskey ? 0 : 1));
			return;
		}

		Timestamp lastOnline = pcd.getLastOnline();
		if (!pcd.isInEditMode() && lastOnline != null && System.currentTimeMillis() - lastOnline.getTime() < (GSConfig.CHARACTER_REENTRY_TIME * 1000)) {
			client.sendPacket(new SM_ENTER_WORLD_CHECK(Msg.REENTRY_TIME));
			return;
		}

		final Player player = PlayerService.getPlayer(objectId, account);

		if (SecurityConfig.DUALBOXING && !player.isStaff()) {
			boolean[] kick = { false };
			World.getInstance().forEachPlayer(new Consumer<>() {

				String pMac = client.getMacAddress() == null || client.getMacAddress().isEmpty() ? "empty" : client.getMacAddress();
				String pHdd = client.getHddSerial() == null || client.getHddSerial().isEmpty() ? "empty" : client.getHddSerial();
				String pIp = client.getIP() == null || client.getIP().isEmpty() ? "empty" : client.getIP();

				@Override
				public void accept(Player visitor) {
					final AionConnection vCon = visitor.getClientConnection();
					if (visitor.equals(player) || visitor.isStaff() || vCon == null)
						return;

					boolean sameMac = pMac.equals(vCon.getMacAddress());
					boolean sameHdd = pHdd.equals(vCon.getHddSerial());
					boolean sameIp = pIp.equals(vCon.getIP());

					if (!sameMac && !sameHdd && !sameIp)
						return;

					StringBuilder sb = new StringBuilder();
					sb.append(sameIp ? "IP " + pIp : "");
					sb.append(sameMac ? " / MAC " + pMac : "");
					sb.append(sameHdd ? " / HDD " + pHdd : "");
					log.info("[Multiclient] Player {} (account {}) and player {} (account {}) share the same {}", player, player.getAccount(), visitor,
						visitor.getAccount(), sb);

					if (SecurityConfig.KICK_DUALBOXING && sameIp && (sameHdd || sameMac)) {
						log.info("[Multiclient] Kicked player " + visitor.getName());
						vCon.close(new SM_QUIT_RESPONSE());
						kick[0] = true;
					}
				}
			});
			if (kick[0])
				client.sendPacket(new SM_ENTER_WORLD_CHECK(Msg.CONNECTION_ERROR));
			return;
		}

		if (!enteringWorld.contains(objectId) && enteringWorld.add(objectId)) {
			try {
				enterWorld(client, player);
			} catch (Throwable ex) {
				player.getController().delete();
				pcd.setOnline(false);
				DAOManager.getDAO(PlayerDAO.class).onlinePlayer(player, false);
				player.setClientConnection(null);
				client.setActivePlayer(null);
				client.sendPacket(new SM_ENTER_WORLD_CHECK(Msg.CONNECTION_ERROR));
				log.error("Error during enter world of " + player, ex);
			} finally {
				enteringWorld.remove(objectId);
			}
		}
	}

	private static void enterWorld(AionConnection client, Player player) {
		Account account = player.getAccount();
		PlayerCommonData pcd = player.getCommonData();

		client.resetPingFailCount();
		activatePassiveSkillEffects(player); // before setClientConnection to avoid packet spam
		player.setClientConnection(client);
		if (!client.setActivePlayer(player))
			throw new IllegalStateException("Couldn't set active player");
		pcd.setOnline(true);
		player.getFriendList().setStatus(Status.ONLINE, pcd);
		DAOManager.getDAO(PlayerDAO.class).onlinePlayer(player, true);
		DAOManager.getDAO(PlayerDAO.class).storeLastOnlineTime(player.getObjectId(), new Timestamp(System.currentTimeMillis()));
		log.info("Player " + player.getName() + " (account " + account.getName() + ") has entered world with " + client.getMacAddress() + " MAC and "
			+ client.getHddSerial() + " HDD serial.");
		pcd.setInEditMode(false);

		World.getInstance().storeObject(player);

		// change players position if he isn't allowed to spawn in the current zone
		if (validateFortressZone(player)) // only check vortex zone if fortress check was ok (otherwise, the player is already set to bind point)
			validateVortexZone(player);

		// if player skipped some levels offline, learn missing skills and stuff
		player.getController().onLevelChange(DAOManager.getDAO(PlayerDAO.class).getOldCharacterLevel(player.getObjectId()), player.getLevel());

		// Energy of Repose must be calculated before sending SM_STATS_INFO
		if (pcd.getLastOnline() != null) {
			long secondsOffline = (System.currentTimeMillis() - pcd.getLastOnline().getTime()) / 1000;
			if (secondsOffline > 10 * 60) // 10 mins offline = 0 salvation points
				pcd.resetSalvationPoints();

			updateEnergyOfRepose(player, secondsOffline);

			if (secondsOffline > 5 * 60)
				pcd.setDp(0);
		}

		client.sendPacket(new SM_UNK_3_5_1());
		StigmaService.onPlayerLogin(player);
		client.sendPacket(new SM_ENTER_WORLD_CHECK());

		InstanceService.onPlayerLogin(player);
		// Update player skills first!!!
		if (player.hasAccess(AdminConfig.GM_SKILLS))
			GMService.getInstance().addGmSkills(player);
		AbyssSkillService.updateSkills(player);
		SplitList<PlayerSkillEntry> skillEntrySplitList = new DynamicServerPacketBodySplitList<>(player.getSkillList().getAllSkills(), false,
			SM_SKILL_LIST.STATIC_BODY_SIZE, SkillEntryWriter.DYNAMIC_BODY_PART_SIZE_CALCULATOR);
		skillEntrySplitList.forEach(part -> PacketSendUtility.sendPacket(player, new SM_SKILL_LIST(part)));
		if (player.getSkillCoolDowns() != null)
			client.sendPacket(new SM_SKILL_COOLDOWN(player.getSkillCoolDowns(), true));

		if (!player.getItemCoolDowns().isEmpty())
			client.sendPacket(new SM_ITEM_COOLDOWN(player.getItemCoolDowns()));

		QuestEngine.getInstance().sendCompletedQuests(player);
		client.sendPacket(new SM_QUEST_LIST(player.getQuestStateList().getUncompletedQuests()));
		client.sendPacket(new SM_TITLE_INFO(pcd.getTitleId()));
		if (pcd.getBonusTitleId() != 0) {
			player.getTitleList().setBonusTitle(pcd.getBonusTitleId());
		}
		client.sendPacket(new SM_MOTION(player.getMotions().getMotions().values()));
		client.sendPacket(new SM_AFTER_TIME_CHECK_4_7_5());// it is also send after enter world check

		byte[] uiSettings = player.getPlayerSettings().getUiSettings();
		byte[] shortcuts = player.getPlayerSettings().getShortcuts();
		byte[] houseBuddies = player.getPlayerSettings().getHouseBuddies();

		if (uiSettings != null)
			client.sendPacket(new SM_UI_SETTINGS(uiSettings, 0));

		if (shortcuts != null)
			client.sendPacket(new SM_UI_SETTINGS(shortcuts, 1));

		if (houseBuddies != null)
			client.sendPacket(new SM_UI_SETTINGS(houseBuddies, 2));

		sendItemInfos(client, player);

		client.sendPacket(new SM_CHANNEL_INFO(player.getPosition()));

		KiskService.getInstance().onLogin(player);
		TeleportService.sendObeliskBindPoint(player);
		TeleportService.sendKiskBindPoint(player);

		AhserionRaid.getInstance().onPlayerLogin(player);

		// ----------------------------- Retail sequence -----------------------------
		client.sendPacket(new SM_PLAYER_SPAWN(player));
		// SM_WEATHER miss on login (but he 'live' in CM_LEVEL_READY.. need investigate)
		client.sendPacket(new SM_GAME_TIME());
		if (player.isLegionMember())
			LegionService.getInstance().onLogin(player);
		sendWarehouseItemInfos(client, player);
		client.sendPacket(new SM_TITLE_INFO(player));
		client.sendPacket(new SM_EMOTION_LIST((byte) 0, player.getEmotions().getEmotions()));
		// SM_BD_UNK h 0
		SiegeService.getInstance().onPlayerLogin(player);
		client.sendPacket(new SM_PRICES());
		if (!player.getCraftCooldowns().isEmpty())
			client.sendPacket(new SM_RECIPE_COOLDOWN(player, 1));
		BindPointTeleportService.onLogin(player);
		client.sendPacket(new SM_FRIEND_LIST());
		client.sendPacket(new SM_BLOCK_LIST());
		if (AutoGroupConfig.AUTO_GROUP_ENABLE) {
			AutoGroupService.getInstance().onPlayerLogin(player);
		}
		client.sendPacket(new SM_INSTANCE_INFO((byte) 2, player));
		client.sendPacket(new SM_ABYSS_RANK(player));
		client.sendPacket(new SM_STATS_INFO(player));
		// ----------------------------- Retail sequence -----------------------------

		if (player.hasAccess(AdminConfig.REVISION_INFO_ON_LOGIN))
			PacketSendUtility.sendMessage(player, VERSION_INFO, ChatType.WHITE);

		if (account.getMembership() > 0 && account.getMembership() <= MembershipConfig.MEMBERSHIP_TYPES.length) {
			String accountType = MembershipConfig.MEMBERSHIP_TYPES[account.getMembership() - 1];
			client.sendPacket(new SM_MESSAGE(0, null, "Your account is " + accountType, ChatType.GOLDEN_YELLOW));
		}

		// Alliance Packet after SetBindPoint
		PlayerAllianceService.onPlayerLogin(player);

		PunishmentService.updatePrisonStatus(player);

		PlayerGroupService.onPlayerLogin(player);
		PetService.getInstance().onPlayerLogin(player);

		// ----------------------------- Retail sequence -----------------------------
		client.sendPacket(new SM_LEGION_DOMINION_LOC_INFO());
		MailService.onPlayerLogin(player);
		HousingBidService.getInstance().onPlayerLogin(player); // must ensure player mailbox is initialized first
		AtreianPassportService.getInstance().onLogin(player);
		sendMacroList(client, player);
		client.sendPacket(new SM_RECIPE_LIST(player.getRecipeList().getRecipeList()));
		BrokerService.getInstance().onPlayerLogin(player);
		HousingService.getInstance().onPlayerLogin(player); // must ensure player mailbox is initialized first
		// ----------------------------- Retail sequence -----------------------------
		if (CustomConfig.ENABLE_SIMPLE_2NDCLASS)
			ClassChangeService.showClassChangeDialog(player);

		GMService.getInstance().onPlayerLogin(player);

		if (player.getAbyssRank().getRank().getId() >= AbyssRankEnum.STAR1_OFFICER.getId()) {
			client.sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_LOSE_COMMON());
			client.sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_LOSE_PERSONAL(player.getName(), player.getAbyssRank().getRank().getGpLossPerDay()));
		}

		// Trigger restore services on login.
		player.getLifeStats().updateCurrentStats();
		player.getObserveController().notifyHPChangeObservers(player.getLifeStats().getCurrentHp());

		if (HTMLConfig.ENABLE_HTML_WELCOME)
			HTMLService.showHTML(player, HTMLCache.getInstance().getHTML("welcome.xhtml"));

		player.getNpcFactions().sendDailyQuest();

		if (HTMLConfig.ENABLE_GUIDES)
			HTMLService.onPlayerLogin(player);

		player.getEquipment().checkRankLimitItems(); // Remove items after offline changed rank

		List<Expirable> expirables = new ArrayList<>();
		for (StorageType st : StorageType.values()) {
			if (st == StorageType.LEGION_WAREHOUSE)
				continue;
			IStorage storage = player.getStorage(st.getId());
			if (storage != null)
				expirables.addAll(storage.getItems());
		}
		expirables.addAll(player.getEquipment().getEquippedItems());
		expirables.addAll(player.getMotions().getMotions().values());
		expirables.addAll(player.getEmotions().getEmotions());
		expirables.addAll(player.getTitleList().getTitles());
		ExpireTimerTask.getInstance().registerExpirables(expirables, player);

		if (player.getActiveHouse() != null) {
			for (HouseObject<?> obj : player.getActiveHouse().getRegistry().getObjects()) {
				if (obj.getPersistentState() != PersistentState.DELETED)
					ExpireTimerTask.getInstance().registerExpirable(obj, player);
			}
		}
		// scheduler periodic update
		player.getController().addTask(TaskId.PLAYER_UPDATE, ThreadPoolManager.getInstance().scheduleAtFixedRate(
			new GeneralUpdateTask(player.getObjectId()), PeriodicSaveConfig.PLAYER_GENERAL * 1000, PeriodicSaveConfig.PLAYER_GENERAL * 1000));
		player.getController().addTask(TaskId.INVENTORY_UPDATE, ThreadPoolManager.getInstance()
			.scheduleAtFixedRate(new ItemUpdateTask(player.getObjectId()), PeriodicSaveConfig.PLAYER_ITEMS * 1000, PeriodicSaveConfig.PLAYER_ITEMS * 1000));

		SurveyService.getInstance().showAvailable(player);
		EventService.getInstance().onPlayerLogin(player);

		if (CraftConfig.DELETE_EXCESS_CRAFT_ENABLE)
			RelinquishCraftStatus.removeExcessCraftStatus(player, false);

		// try to send bonus pack (if mailbox was full on lvlup)
		BonusPackService.getInstance().addPlayerCustomReward(player);
		FactionPackService.getInstance().addPlayerCustomReward(player);
		VeteranRewardService.getInstance().tryReward(player);

		PvpMapService.getInstance().onLogin(player);
	}

	@SuppressWarnings("lossy-conversions")
	private static void updateEnergyOfRepose(Player player, long secondsOffline) {
		player.getCommonData().updateMaxRepose();
		if (player.getCommonData().isReadyForReposeEnergy() && secondsOffline > 4 * 3600) { // more than 4 hours offline: start counting Repose Energy addition
			double hours = secondsOffline / 3600d;
			// 48 hours offline = 100% Repose Energy (~1% each 30mins source: http://forums.na.aiononline.com/na/showthread.php?t=105940)
			long addReposeEnergy = Math.round((hours / 48) * player.getCommonData().getMaxReposeEnergy());
			// Additional Energy of Repose bonus if inside house
			House house = player.getActiveHouse();
			if (house != null) {
				HouseAddress hPos = house.getAddress();
				if (player.getWorldId() == hPos.getMapId()
					&& PositionUtil.isInRange(player.getX(), player.getY(), player.getZ(), hPos.getX(), hPos.getY(), hPos.getZ(), 7))
					addReposeEnergy *= house.getHouseType() == HouseType.STUDIO ? 1.05f : 1.10f; // apartment = 5% bonus, other houses 10%
			}
			player.getCommonData().addReposeEnergy(addReposeEnergy);
		}
	}

	private static void activatePassiveSkillEffects(Player player) {
		for (PlayerSkillEntry skillEntry : player.getSkillList().getAllSkills()) {
			SkillTemplate skillTemplate = DataManager.SKILL_DATA.getSkillTemplate(skillEntry.getSkillId());
			if (skillTemplate.isPassive())
				SkillEngine.getInstance().applyEffectDirectly(skillTemplate, skillEntry.getSkillLevel(), player, player);
		}
	}

	private static boolean validateFortressZone(Player player) {
		FortressLocation fortress = SiegeService.getInstance().findFortress(player.getWorldId(), player.getX(), player.getY(), player.getZ());
		if (fortress != null && fortress.isVulnerable() && fortress.isEnemy(player)) {
			long lastOnlineMillis = player.getCommonData().getLastOnline() == null ? 0 : player.getCommonData().getLastOnline().getTime();
			// only relocate if the player logged out before siege start (online enemies automatically get teleported outside the fortress)
			if (lastOnlineMillis < SiegeService.getInstance().getSiege(fortress).getStartTime()) {
				BindPointPosition bind = player.getBindPoint();
				if (bind != null) {
					World.getInstance().setPosition(player, bind.getMapId(), bind.getX(), bind.getY(), bind.getZ(), bind.getHeading());
				} else {
					PlayerInitialData.LocationData start = DataManager.PLAYER_INITIAL_DATA.getSpawnLocation(player.getRace());
					World.getInstance().setPosition(player, start.getMapId(), start.getX(), start.getY(), start.getZ(), start.getHeading());
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the player is allowed to be in the current vortex zone. He will be sent to the locations home point if not.
	 */
	private static void validateVortexZone(Player player) {
		VortexLocation loc = VortexService.getInstance().getLocationByWorld(player.getWorldId());
		if (loc != null && player.getRace().equals(loc.getInvadersRace())) {
			if (loc.isInsideLocation(player) && loc.isActive() && loc.getVortexController().getPassedPlayers().containsKey(player.getObjectId()))
				return;

			int mapId = loc.getHomeWorldId();
			float x = loc.getHomePoint().getX();
			float y = loc.getHomePoint().getY();
			float z = loc.getHomePoint().getZ();
			byte h = loc.getHomePoint().getHeading();
			World.getInstance().setPosition(player, mapId, x, y, z, h);
		}
	}

	private static void sendItemInfos(AionConnection client, Player player) {
		player.setCubeLimit();
		player.setWarehouseLimit();
		// items
		Storage inventory = player.getInventory();
		List<Item> allItems = new ArrayList<>();
		if (inventory.getKinah() == 0) {
			inventory.increaseKinah(0); // create an empty object with value 0
		}
		allItems.add(inventory.getKinahItem()); // always included even with 0 count, and first in the packet !
		allItems.addAll(player.getEquipment().getEquippedItems());
		allItems.addAll(inventory.getItems());

		SplitList<Item> inventoryItemSplitList = new FixedElementCountSplitList<>(allItems, true, 10);
		inventoryItemSplitList.forEach(part -> client.sendPacket(new SM_INVENTORY_INFO(part.isFirst(), part, player)));
		client.sendPacket(new SM_INVENTORY_INFO(false, Collections.emptyList(), player));
	}

	private static void sendWarehouseItemInfos(AionConnection client, Player player) {
		WarehouseService.sendWarehouseInfo(player, true);
		// from 30 to 49, from 60 to 79
		for (int i = StorageType.PET_BAG_MIN - 2; i <= StorageType.HOUSE_WH_MAX; i++) {
			if (i >= 50 && i < StorageType.HOUSE_WH_MIN)
				continue;
			IStorage storage = player.getStorage(i);
			if (storage == null || storage.getItemsWithKinah().size() == 0) {
				client.sendPacket(new SM_WAREHOUSE_INFO(null, i, 0, true, player));
				continue;
			}
			SplitList<Item> warehouseItemSplitList = new FixedElementCountSplitList<>(storage.getItemsWithKinah(), true, 10);
			int storageType = i;
			warehouseItemSplitList.forEach(part -> client.sendPacket(new SM_WAREHOUSE_INFO(part, storageType, 0, part.isFirst(), player)));
			client.sendPacket(new SM_WAREHOUSE_INFO(null, storageType, 0, false, player));
			client.sendPacket(new SM_WAREHOUSE_INFO(null, i, 0, false, player));
		}
	}

	private static void sendMacroList(AionConnection client, Player player) {
		client.sendPacket(new SM_MACRO_LIST(player, false));
		if (player.getMacroList().getSize() > 7)
			client.sendPacket(new SM_MACRO_LIST(player, true));
	}
}

class GeneralUpdateTask implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(GeneralUpdateTask.class);
	private final int playerId;

	GeneralUpdateTask(int playerId) {
		this.playerId = playerId;
	}

	@Override
	public void run() {
		Player player = World.getInstance().getPlayer(playerId);
		if (player != null) {
			try {
				DAOManager.getDAO(AbyssRankDAO.class).storeAbyssRank(player);
				DAOManager.getDAO(PlayerSkillListDAO.class).storeSkills(player);
				DAOManager.getDAO(PlayerQuestListDAO.class).store(player);
				DAOManager.getDAO(PlayerDAO.class).storePlayer(player);
				for (House house : player.getHouses())
					house.save();
			} catch (Exception ex) {
				log.error("Exception during periodic saving of player " + player.getName(), ex);
			}
		}
	}
}

class ItemUpdateTask implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(ItemUpdateTask.class);
	private final int playerId;

	ItemUpdateTask(int playerId) {
		this.playerId = playerId;
	}

	@Override
	public void run() {
		Player player = World.getInstance().getPlayer(playerId);
		if (player != null) {
			try {
				DAOManager.getDAO(InventoryDAO.class).store(player);
				DAOManager.getDAO(ItemStoneListDAO.class).save(player);
			} catch (Exception ex) {
				log.error("Exception during periodic saving of player items " + player.getName(), ex);
			}
		}
	}
}
