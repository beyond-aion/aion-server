package com.aionemu.gameserver.services;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.dao.FactionPackDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Estrayl, Neon
 */
public class FactionPackService {

	private static final FactionPackService INSTANCE = new FactionPackService();
	private final LocalDateTime elyosMinCreationTime = LocalDateTime.of(2020, Month.SEPTEMBER, 14, 0, 0, 0);
	private final LocalDateTime elyosMaxCreationTime = LocalDateTime.of(2020, Month.SEPTEMBER, 26, 23, 59, 59);
	private final LocalDateTime asmodianMinCreationTime = LocalDateTime.of(2022, Month.JUNE, 18, 0, 0, 0);
	private final LocalDateTime asmodianMaxCreationTime = LocalDateTime.of(2022, Month.JULY, 19, 23, 59, 59);
	private final List<RewardItem> rewards = new ArrayList<>();

	public static FactionPackService getInstance() {
		return INSTANCE;
	}

	private FactionPackService() {
		rewards.add(new RewardItem(186000236, 500)); // Blood Mark
		rewards.add(new RewardItem(162002030, 250)); // [Event] Premium Restoration Serum
		rewards.add(new RewardItem(162000023, 100)); // Greater Healing Potion
		rewards.add(new RewardItem(166000195, 50)); // Epsilon Enchantment Stone
		rewards.add(new RewardItem(169630007, 1)); // [Expand Card] Expand Cube Ticket (lvl 4)
		rewards.add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
	}

	public void addPlayerCustomReward(Player player) {
		if (rewards.isEmpty() || player.getLevel() != 65 || (player.getCommonData().getMailboxLetters() + rewards.size() > 100))
			return;
		if (player.getRace() == Race.ASMODIANS)
			sendRewards(player, asmodianMinCreationTime, asmodianMaxCreationTime);
		else
			sendRewards(player, elyosMinCreationTime, elyosMaxCreationTime);
	}

	private void sendRewards(Player player, LocalDateTime minCreationTime, LocalDateTime maxCreationTime) {
		LocalDateTime creationTime = ServerTime.ofEpochMilli(player.getAccount().getCreationDate()).toLocalDateTime();
		if (minCreationTime == null || creationTime.isBefore(minCreationTime))
			return;
		if (maxCreationTime != null && creationTime.isAfter(maxCreationTime))
			return;
		int accountId = player.getAccount().getId();
		if (FactionPackDAO.loadReceivingPlayer(accountId) > 0)
			return;
		if (!FactionPackDAO.storeReceivingPlayer(accountId, player.getObjectId()))
			return;
		for (RewardItem e : rewards) {
			ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(e.getId());
			if (template != null && template.getRace() == player.getOppositeRace())
				continue;
			SystemMailService.sendMail(
				"Beyond Aion", player.getName(), "Faction Pack", "Greetings Daeva!\n\n"
					+ "In gratitude for your decision to join this faction we prepared an additional item pack.\n\n" + "Enjoy your stay on Beyond Aion!",
				e.getId(), e.getCount(), 0, LetterType.EXPRESS);
		}
	}
}
