package com.aionemu.gameserver.services;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.dao.BonusPackDAO;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.mail.SystemMailService;

/**
 * @author Estrayl, Neon
 */
public class BonusPackService {

	private static final BonusPackService INSTANCE = new BonusPackService();
	private final HashMap<Integer, Integer> rewards = new HashMap<>();

	public static BonusPackService getInstance() {
		return INSTANCE;
	}

	private BonusPackService() {
						    // itemId, count

		rewards.put(186000242, 15); // Ceramium Medal
		rewards.put(186000130, 6500); // Crucible Insignia
		rewards.put(186000051, 5); // Major Ancient Crown
		rewards.put(166000195, 15); // Epsilon Enchantment Stone

		rewards.put(186000236, 250); // Blood Mark
		rewards.put(186000237, 4500); // Ancient Coin
		rewards.put(186000409, 150); // Daeva's Respite Coin

		rewards.put(188052562, 5); // Scroll Bundle
		rewards.put(190100051, 1); // Flying Pagati
	}

	public void addPlayerCustomReward(Player player) {
		if (rewards == null || rewards.isEmpty())
			return;

		if (player.getLevel() != 65)
			return;

		if (player.getCommonData().getMailboxLetters() + rewards.size() > 100)
			return;

		int accountId = player.getAccount().getId();
		if (BonusPackDAO.loadReceivingPlayer(accountId) > 0)
			return;

		if (!BonusPackDAO.storeReceivingPlayer(accountId, player.getObjectId()))
			return;

		for (Map.Entry<Integer, Integer> e : rewards.entrySet()) {
			SystemMailService.sendMail("Beyond Aion",	player.getName(), "Bonus Pack",
				"Greetings Daeva!\n\n" + "You have reached level 65 with your first character and therefore we have a special something for you."
					+ " In gratitude for your support we have prepared a package with valuable items for you.\n\n"
					+ "Enjoy your stay on Beyond Aion!", e.getKey(), e.getValue(), 0, LetterType.EXPRESS);
		}
	}
}
