package com.aionemu.gameserver.services;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.FactionPackDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.mail.SystemMailService;

/**
 * @author Estrayl
 */
public class FactionPackService {

	private static final FactionPackService INSTANCE = new FactionPackService();
	private final LocalDateTime elyosMinCreationTime = LocalDateTime.of(2015, Month.DECEMBER, 7, 5, 0, 0);
	private final LocalDateTime elyosMaxCreationTime = LocalDateTime.of(2016, Month.JANUARY, 3, 4, 0, 0);
	private final LocalDateTime asmodianMinCreationTime = LocalDateTime.of(2016, Month.JANUARY, 17, 2, 0, 0);
	private final LocalDateTime asmodianMaxCreationTime = LocalDateTime.of(2016, Month.MARCH, 15, 3, 0, 0);
	private final FactionPackDAO dao = DAOManager.getDAO(FactionPackDAO.class);
	private final HashMap<Integer, Integer> rewards = new HashMap<>();

	public static FactionPackService getInstance() {
		return INSTANCE;
	}

	private FactionPackService() {
		// itemId, count
		rewards.put(186000236, 230); // Blood Mark
		rewards.put(162000124, 250); // Superior Restoration Serum
		rewards.put(162000023, 100); // Greater Healing Potion
		rewards.put(186000137, 1500); // Courage Insignia
		rewards.put(186000130, 3500); // Crucible Insignia

		rewards.put(160010121, 10); // Inquin Bonbon
		rewards.put(160010122, 10); // Inquin Bonbon
	}

	public void addPlayerCustomReward(Player player) {
		if (rewards == null || rewards.isEmpty())
			return;
		if (player.getLevel() != 65)
			return;
		if (player.getCommonData().getMailboxLetters() + rewards.size() > 100)
			return;
		if (player.getRace() == Race.ASMODIANS) {
			addAsmodianCustomReward(player);
			return;
		}
		LocalDateTime creationTime = player.getPlayerAccount().getPlayerAccountData(player.getObjectId()).getCreationDate().toLocalDateTime();
		if (creationTime.isBefore(elyosMinCreationTime))
			return;
		if (creationTime.isAfter(elyosMaxCreationTime))
			return;
		int accountId = player.getPlayerAccount().getId();
		if (dao.loadReceivingPlayer(accountId) > 0)
			return;
		if (!dao.storeReceivingPlayer(accountId, player.getObjectId()))
			return;
		for (Map.Entry<Integer, Integer> e : rewards.entrySet()) {
			SystemMailService.getInstance().sendMail(
				"Beyond Aion",
				player.getName(),
				"Faction Pack",
				"Greetings Daeva!\n\n" + "In gratitude for your decision to join the Elyos faction we prepared an additional item pack.\n\n"
					+ "Enjoy your stay on Beyond Aion!", e.getKey(), e.getValue(), 0, LetterType.EXPRESS);
		}
	}

	public void addAsmodianCustomReward(Player player) {
		LocalDateTime creationTime = player.getPlayerAccount().getPlayerAccountData(player.getObjectId()).getCreationDate().toLocalDateTime();
		if (creationTime.isBefore(asmodianMinCreationTime))
			return;
		if (creationTime.isAfter(asmodianMaxCreationTime))
			return;
		int accountId = player.getPlayerAccount().getId();
		if (dao.loadReceivingPlayer(accountId) > 0)
			return;
		if (!dao.storeReceivingPlayer(accountId, player.getObjectId()))
			return;
		for (Map.Entry<Integer, Integer> e : rewards.entrySet()) {
			SystemMailService.getInstance().sendMail(
				"Beyond Aion",
				player.getName(),
				"Faction Pack",
				"Greetings Daeva!\n\n" + "In gratitude for your decision to join the Asmodian faction we prepared an additional item pack.\n\n"
					+ "Enjoy your stay on Beyond Aion!", e.getKey(), e.getValue(), 0, LetterType.EXPRESS);
		}
	}
}
