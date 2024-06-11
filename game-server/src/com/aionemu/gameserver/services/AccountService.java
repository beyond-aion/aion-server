package com.aionemu.gameserver.services;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.GameServer;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.LegionMemberDAO;
import com.aionemu.gameserver.dao.PlayerAppearanceDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.dao.PlayerPunishmentsDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.AccountTime;
import com.aionemu.gameserver.model.account.CharacterBanInfo;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.PlayerAppearance;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.player.PlayerService;

/**
 * This class is a front-end for daos and it's responsibility is to retrieve the Account objects
 *
 * @author Luno, cura
 */
public class AccountService {

	private static final Logger log = LoggerFactory.getLogger(AccountService.class);

	/**
	 * Returns {@link Account} object that has given id.
	 */
	public static Account getAccount(int accountId, String accountName, long creationDate, AccountTime accountTime, byte accessLevel, byte membership,
		long toll, String allowedHddSerial) {
		log.debug("[AS] request for account: " + accountId);

		Account account = loadAccount(accountId);
		account.setName(accountName);
		account.setCreationDate(creationDate);
		account.setAccountTime(accountTime);
		account.setAccessLevel(accessLevel);
		account.setMembership(membership);
		account.setToll(toll);
		account.setAllowedHddSerial(allowedHddSerial);
		removeDeletedCharacters(account);
		return account;
	}

	/**
	 * Removes from db characters that should be deleted (their deletion time has passed).
	 */
	public static void removeDeletedCharacters(Account account) {
		/* Removes chars that should be removed */
		Iterator<PlayerAccountData> it = account.iterator();
		while (it.hasNext()) {
			PlayerAccountData pad = it.next();
			Race race = pad.getPlayerCommonData().getRace();
			long deletionTime = pad.getDeletionDate() == null ? 0 : pad.getDeletionDate().getTime();
			if (deletionTime != 0 && deletionTime <= System.currentTimeMillis()) {
				it.remove();
				account.decrementCountOf(race);
				PlayerService.deletePlayerFromDB(pad.getPlayerCommonData().getPlayerObjId());
				if (GSConfig.ENABLE_RATIO_LIMITATION && pad.getPlayerCommonData().getLevel() >= GSConfig.RATIO_MIN_REQUIRED_LEVEL) {
					if (account.getNumberOf(race) == 0) {
						GameServer.updateRatio(pad.getPlayerCommonData().getRace(), -1);
					}
				}
			}
		}
		if (account.isEmpty()) {
			InventoryDAO.deleteAccountWH(account.getId());
			loadAccountWarehouse(account);
		}
	}

	public static Account loadAccount(int accountId) {
		Account account = new Account(accountId);
		List<Integer> playerIdList = PlayerDAO.getPlayerOidsOnAccount(accountId);
		for (int playerId : playerIdList)
			account.addPlayerAccountData(loadPlayerAccountData(playerId));
		account.setAccountWarehouse(loadAccountWarehouse(account));
		return account;
	}

	public static PlayerAccountData loadPlayerAccountData(int playerId) {
		PlayerCommonData playerCommonData = PlayerDAO.loadPlayerCommonData(playerId);
		CharacterBanInfo cbi = PlayerPunishmentsDAO.getCharBanInfo(playerId);
		PlayerAppearance appereance = PlayerAppearanceDAO.load(playerId);
		LegionMember legionMember = LegionMemberDAO.loadLegionMember(playerId);
		// Load only equipment and its stones to display on character selection screen
		List<Item> equipment = InventoryDAO.loadEquipment(playerId);

		PlayerAccountData playerAccData = new PlayerAccountData(playerCommonData, appereance, cbi, equipment, legionMember);
		PlayerDAO.setCreationDeletionTime(playerAccData);
		return playerAccData;
	}

	public static Storage loadAccountWarehouse(Account account) {
		Storage wh = InventoryDAO.loadStorage(account.getId(), StorageType.ACCOUNT_WAREHOUSE);
		ItemService.loadItemStones(wh.getItems());
		return wh;
	}
}
