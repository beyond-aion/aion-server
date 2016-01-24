package com.aionemu.gameserver.services.player;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.utils.info.VersionInfo;
import com.aionemu.commons.utils.info.VersionInfoUtil;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.cache.HTMLCache;
import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.configs.main.CraftConfig;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.configs.main.HTMLConfig;
import com.aionemu.gameserver.configs.main.PeriodicSaveConfig;
import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dao.PlayerPasskeyDAO;
import com.aionemu.gameserver.dao.PlayerPunishmentsDAO;
import com.aionemu.gameserver.dao.PlayerQuestListDAO;
import com.aionemu.gameserver.dao.PlayerSkillListDAO;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.CharacterBanInfo;
import com.aionemu.gameserver.model.account.CharacterPasskey.ConnectType;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.gameobjects.player.emotion.Emotion;
import com.aionemu.gameserver.model.gameobjects.player.motion.Motion;
import com.aionemu.gameserver.model.gameobjects.player.title.Title;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.items.storage.IStorage;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.model.templates.housing.HouseAddress;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ABYSS_RANK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AFTER_TIME_CHECK_4_7_5;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BLOCK_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHANNEL_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHARACTER_SELECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUBE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ENTER_WORLD_CHECK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ENTER_WORLD_CHECK.Msg;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FRIEND_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GAME_TIME;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_COOLDOWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MACRO_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_SPAWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PRICES;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUIT_RESPONSE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECIPE_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_COOLDOWN;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_LIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TITLE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UI_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UNK_3_5_1;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WAREHOUSE_INFO;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.AtreianPassportService;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.services.BonusPackService;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.services.ClassChangeService;
import com.aionemu.gameserver.services.DisputeLandService;
import com.aionemu.gameserver.services.EventService;
import com.aionemu.gameserver.services.FactionPackService;
import com.aionemu.gameserver.services.HTMLService;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.KiskService;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.PunishmentService;
import com.aionemu.gameserver.services.PunishmentService.PunishmentType;
import com.aionemu.gameserver.services.SerialKillerService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.StigmaService;
import com.aionemu.gameserver.services.SurveyService;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.services.WarehouseService;
import com.aionemu.gameserver.services.abyss.AbyssSkillService;
import com.aionemu.gameserver.services.craft.RelinquishCraftStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.mail.MailService;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionInstance;
import com.aionemu.gameserver.services.teleport.BindPointTeleportService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.services.toypet.PetService;
import com.aionemu.gameserver.services.transfers.PlayerTransferService;
import com.aionemu.gameserver.taskmanager.tasks.ExpireTimerTask;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.GMService;
import com.aionemu.gameserver.utils.collections.ListSplitter;
import com.aionemu.gameserver.utils.rates.Rates;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author ATracer
 * @modified Neon
 */
public final class PlayerEnterWorldService {

	private static final Logger log = LoggerFactory.getLogger("GAMECONNECTION_LOG");
	private static final VersionInfo gsVer = VersionInfoUtil.getVersionInfo(GameServer.class);
	private static final String welcomeInfo = GSConfig.SERVER_MOTD;
	private static final String versionInfo = "Server Revision: " + gsVer.getRevision() + ", built on " + gsVer.getDate();
	private static final ConcurrentLinkedQueue<Integer> enteringWorld = new ConcurrentLinkedQueue<>();

	public static final void enterWorld(final AionConnection client, int objectId) {
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

		if (World.getInstance().findPlayer(objectId) != null) {
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
		if (player == null) {
			log.warn("Player enterWorld fail: couldn't load player with obj ID {}, account ID {}.", objectId, account.getId());
			client.sendPacket(new SM_ENTER_WORLD_CHECK(Msg.CONNECTION_ERROR));
			return;
		}

		if (SecurityConfig.DUALBOXING && !player.isGM()) {
			boolean[] kick = { false };
			World.getInstance().doOnAllPlayers(new Visitor<Player>() {

				String pMac = client.getMacAddress() == null || client.getMacAddress().isEmpty() ? "empty" : client.getMacAddress();
				String pHdd = client.getHddSerial() == null || client.getHddSerial().isEmpty() ? "empty" : client.getHddSerial();
				String pIp = client.getIP() == null || client.getIP().isEmpty() ? "empty" : client.getIP();

				@Override
				public void visit(Player visitor) {
					final AionConnection vCon = visitor.getClientConnection();
					if (visitor.equals(player) || visitor.isGM() || vCon == null)
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
					log.info("[Multiclient] Player {} (account {}) and player {} (account {}) share the same {}", player.getName(), account.getName(),
						visitor.getName(), vCon.getAccount().getName(), sb);

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
			if (!client.setActivePlayer(player)) { // set active player as soon as possible, so the reentry from edit mode doesn't throw NPEs
				log.warn("Player enterWorld fail: couldn't set active player for obj ID {}, Player: {}", objectId, player);
				enteringWorld.remove(objectId);
				client.sendPacket(new SM_ENTER_WORLD_CHECK(Msg.CONNECTION_ERROR));
				return;
			}

			ThreadPoolManager.getInstance().execute(new Runnable() {

				@Override
				public void run() {
					try {
						enterWorld(client, player);
					} catch (Throwable ex) {
						player.getController().delete();
						pcd.setOnline(false);
						DAOManager.getDAO(PlayerDAO.class).onlinePlayer(player, false);
						player.setClientConnection(null);
						client.setActivePlayer(null);
						client.sendPacket(new SM_ENTER_WORLD_CHECK(Msg.CONNECTION_ERROR));
						log.error("Error during enter world " + objectId, ex);
					} finally {
						enteringWorld.remove(objectId);
					}
				}
			});
		}
	}

	private static final void enterWorld(AionConnection client, Player player) {
		Account account = player.getPlayerAccount();
		PlayerCommonData pcd = player.getCommonData();

		player.setClientConnection(client);
		pcd.setOnline(true);
		player.getFriendList().setStatus(Status.ONLINE, pcd);
		DAOManager.getDAO(PlayerDAO.class).onlinePlayer(player, true);
		DAOManager.getDAO(PlayerDAO.class).storeLastOnlineTime(player.getObjectId(), new Timestamp(System.currentTimeMillis()));
		log.info("Player " + player.getName() + " (account " + account.getName() + ") has entered world with " + client.getMacAddress() + " MAC and "
			+ client.getHddSerial() + " HDD serial.");
		pcd.setInEditMode(false);

		World.getInstance().storeObject(player);

		// if player skipped some levels offline, learn missing skills and stuff
		player.getController().onLevelChange(DAOManager.getDAO(PlayerDAO.class).getOldCharacterLevel(player.getObjectId()), player.getLevel());

		/**
		 * Energy of Repose must be calculated before sending SM_STATS_INFO
		 */
		if (pcd.getLastOnline() != null) {
			long secondsOffline = (System.currentTimeMillis() - pcd.getLastOnline().getTime()) / 1000;
			if (secondsOffline > 10 * 60) // 10 mins offline = 0 salvation points
				pcd.resetSalvationPoints();

			pcd.updateMaxRepose();
			if (pcd.isReadyForReposeEnergy() && secondsOffline > 4 * 3600) { // more than 4 hours offline: start counting Repose Energy addition
				double hours = secondsOffline / 3600d;
				// 48 hours offline = 100% Repose Energy (~1% each 30mins source: http://forums.na.aiononline.com/na/showthread.php?t=105940)
				long addReposeEnergy = Math.round((hours / 48) * pcd.getMaxReposeEnergy());
				// Additional Energy of Repose bonus if inside house
				House house = player.getActiveHouse();
				if (house != null) {
					HouseAddress hPos = house.getAddress();
					if (player.getWorldId() == hPos.getMapId() && MathUtil.isIn3dRange(player.getX(), player.getY(), player.getZ(), hPos.getX(), hPos.getY(), hPos.getZ(), 7))
						addReposeEnergy *= house.getHouseType() == HouseType.STUDIO ? 1.05f : 1.10f; // apartment = 5% bonus, other houses 10%
				}
				pcd.addReposeEnergy(addReposeEnergy);
			}

			if (secondsOffline > 5 * 60)
				pcd.setDp(0);
		}

		client.sendPacket(new SM_UNK_3_5_1());
		StigmaService.onPlayerLogin(player);
		client.sendPacket(new SM_ENTER_WORLD_CHECK());

		InstanceService.onPlayerLogin(player);
		// Update player skills first!!!
		AbyssSkillService.updateSkills(player);
		// TODO: check the split size
		client.sendPacket(new SM_SKILL_LIST(player.getSkillList().getAllSkills()));

		if (player.getSkillCoolDowns() != null)
			client.sendPacket(new SM_SKILL_COOLDOWN(player.getSkillCoolDowns(), true));

		if (player.getItemCoolDowns() != null)
			client.sendPacket(new SM_ITEM_COOLDOWN(player.getItemCoolDowns()));

		QuestEngine qe = QuestEngine.getInstance();
		qe.sendCompletedQuests(player);
		client.sendPacket(new SM_QUEST_LIST(qe.getQuestList(player)));
		client.sendPacket(new SM_TITLE_INFO(pcd.getTitleId()));
		QuestEngine.getInstance().onLvlUp(new QuestEnv(null, player, 0, 0));
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
		TeleportService2.sendSetBindPoint(player);

		// Without player spawn initialization can't access to his mapRegion for chk below
		World.getInstance().preSpawn(player);
		player.getController().validateLoginZone();
		VortexService.getInstance().validateLoginZone(player);

		AhserionInstance.getInstance().onPlayerLogin(player);

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
		// SM_A5_UNK ch 1 0
		// SM_A5_UNK ch 0 0
		BindPointTeleportService.onLogin(player);
		client.sendPacket(new SM_FRIEND_LIST());
		client.sendPacket(new SM_BLOCK_LIST());
		if (AutoGroupConfig.AUTO_GROUP_ENABLE) {
			AutoGroupService.getInstance().onPlayerLogin(player);
		}
		client.sendPacket(new SM_INSTANCE_INFO((byte) 2, player));
		DisputeLandService.getInstance().onLogin(player);
		client.sendPacket(new SM_ABYSS_RANK(player.getAbyssRank()));
		client.sendPacket(new SM_STATS_INFO(player));
		// ----------------------------- Retail sequence -----------------------------

		// Intro message
		if (welcomeInfo != null && !welcomeInfo.isEmpty())
			PacketSendUtility.sendWhiteMessage(player, welcomeInfo);

		if (GSConfig.SERVER_MOTD_DISPLAY_REV && versionInfo != null && !versionInfo.isEmpty())
			PacketSendUtility.sendWhiteMessage(player, versionInfo);

		player.setRates(Rates.getRatesFor(account.getMembership()));
		if (CustomConfig.PREMIUM_NOTIFY && account.getMembership() > 0) {
			String accountType = "";
			switch (account.getMembership()) {
				case 1:
					accountType = "premium";
					break;
				case 2:
					accountType = "VIP";
					break;
			}
			client.sendPacket(new SM_MESSAGE(0, null, "Your account is " + accountType, ChatType.GOLDEN_YELLOW));
		}

		// Alliance Packet after SetBindPoint
		PlayerAllianceService.onPlayerLogin(player);

		if (player.isInPrison())
			PunishmentService.updatePrisonStatus(player);

		if (player.isNotGatherable())
			PunishmentService.updateGatherableStatus(player);

		PlayerGroupService.onPlayerLogin(player);
		PetService.getInstance().onPlayerLogin(player);

		// ----------------------------- Retail sequence -----------------------------
		MailService.getInstance().onPlayerLogin(player);
		AtreianPassportService.getInstance().onLogin(player);
		HousingService.getInstance().onPlayerLogin(player);
		sendMacroList(client, player);
		client.sendPacket(new SM_RECIPE_LIST(player.getRecipeList().getRecipeList()));
		BrokerService.getInstance().onPlayerLogin(player);
		// ----------------------------- Retail sequence -----------------------------
		if (AutoGroupConfig.AUTO_GROUP_ENABLE) {
			AutoGroupService.getInstance().onPlayerLogin(player);
		}
		ClassChangeService.showClassChangeDialog(player);

		GMService.getInstance().onPlayerLogin(player);

		if (player.getAbyssRank().getRank().getId() >= AbyssRankEnum.STAR1_OFFICER.getId()) {
			client.sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_LOSE_COMMON);
			client.sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GLORY_POINT_LOSE_PERSONAL(player.getName(), player.getAbyssRank().getRank().getGpLossPerDay()));
		}

		/**
		 * Trigger restore services on login.
		 */
		player.getLifeStats().updateCurrentStats();
		player.getObserveController().notifyHPChangeObservers(player.getLifeStats().getCurrentHp());
		SerialKillerService.getInstance().onLogin(player);

		if (HTMLConfig.ENABLE_HTML_WELCOME)
			HTMLService.showHTML(player, HTMLCache.getInstance().getHTML("welcome.xhtml"));

		player.getNpcFactions().sendDailyQuest();

		if (HTMLConfig.ENABLE_GUIDES)
			HTMLService.onPlayerLogin(player);

		for (StorageType st : StorageType.values()) {
			if (st == StorageType.LEGION_WAREHOUSE)
				continue;
			IStorage storage = player.getStorage(st.getId());
			if (storage != null) {
				for (Item item : storage.getItemsWithKinah())
					if (item.getExpireTime() > 0)
						ExpireTimerTask.getInstance().addTask(item, player);
			}
		}

		for (Item item : player.getEquipment().getEquippedItems())
			if (item.getExpireTime() > 0)
				ExpireTimerTask.getInstance().addTask(item, player);

		player.getEquipment().checkRankLimitItems(); // Remove items after offline changed rank

		for (Motion motion : player.getMotions().getMotions().values()) {
			if (motion.getExpireTime() != 0) {
				ExpireTimerTask.getInstance().addTask(motion, player);
			}
		}

		for (Emotion emotion : player.getEmotions().getEmotions()) {
			if (emotion.getExpireTime() != 0) {
				ExpireTimerTask.getInstance().addTask(emotion, player);
			}
		}

		for (Title title : player.getTitleList().getTitles()) {
			if (title.getExpireTime() != 0) {
				ExpireTimerTask.getInstance().addTask(title, player);
			}
		}

		if (player.getHouseRegistry() != null) {
			for (HouseObject<?> obj : player.getHouseRegistry().getObjects()) {
				if (obj.getPersistentState() == PersistentState.DELETED)
					continue;
				if (obj.getObjectTemplate().getUseDays() > 0)
					ExpireTimerTask.getInstance().addTask(obj, player);
			}
		}
		// scheduler periodic update
		player.getController().addTask(
			TaskId.PLAYER_UPDATE,
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new GeneralUpdateTask(player.getObjectId()), PeriodicSaveConfig.PLAYER_GENERAL * 1000,
				PeriodicSaveConfig.PLAYER_GENERAL * 1000));
		player.getController().addTask(
			TaskId.INVENTORY_UPDATE,
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new ItemUpdateTask(player.getObjectId()), PeriodicSaveConfig.PLAYER_ITEMS * 1000,
				PeriodicSaveConfig.PLAYER_ITEMS * 1000));

		SurveyService.getInstance().showAvailable(player);
		EventService.getInstance().onPlayerLogin(player);

		if (CraftConfig.DELETE_EXCESS_CRAFT_ENABLE)
			RelinquishCraftStatus.removeExcessCraftStatus(player, false);

		PlayerTransferService.getInstance().onEnterWorld(player);

		// try to send bonus pack (if mailbox was full on lvlup)
		BonusPackService.getInstance().addPlayerCustomReward(player);
		FactionPackService.getInstance().addPlayerCustomReward(player);
	}

	/**
	 * @param client
	 * @param player
	 */
	// TODO! this method code is really odd [Nemesiss]
	private static void sendItemInfos(AionConnection client, Player player) {
		player.setCubeLimit();
		player.setWarehouseLimit();
		// items
		Storage inventory = player.getInventory();
		List<Item> allItems = new FastTable<Item>();
		if (inventory.getKinah() == 0) {
			inventory.increaseKinah(0); // create an empty object with value 0
		}
		allItems.add(inventory.getKinahItem()); // always included even with 0 count, and first in the packet !
		allItems.addAll(player.getEquipment().getEquippedItems());
		allItems.addAll(inventory.getItems());

		ListSplitter<Item> splitter = new ListSplitter<>(allItems, 10, true);
		while (splitter.hasMore()) {
			client.sendPacket(new SM_INVENTORY_INFO(splitter.isFirst(), splitter.getNext(), player));
		}
		client.sendPacket(new SM_INVENTORY_INFO(false, new FastTable<>(), player));
		client.sendPacket(SM_CUBE_UPDATE.stigmaSlots(player.getCommonData().getAdvancedStigmaSlotSize()));
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
			ListSplitter<Item> splitter = new ListSplitter<>(storage.getItemsWithKinah(), 10, true);
			while (splitter.hasMore()) {
				client.sendPacket(new SM_WAREHOUSE_INFO(splitter.getNext(), i, 0, splitter.isFirst(), player));
			}
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
		Player player = World.getInstance().findPlayer(playerId);
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
		Player player = World.getInstance().findPlayer(playerId);
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
