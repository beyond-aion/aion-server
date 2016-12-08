package com.aionemu.gameserver.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.FactionPackDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.time.ServerTime;

import javolution.util.FastTable;

/**
 * @author Estrayl
 * @modified Neon
 */
public class FactionPackService {

	private static final FactionPackService INSTANCE = new FactionPackService();
	private final LocalDateTime elyosMinCreationTime = LocalDateTime.of(2015, Month.DECEMBER, 7, 5, 0, 0);
	private final LocalDateTime elyosMaxCreationTime = LocalDateTime.of(2016, Month.JANUARY, 3, 4, 0, 0);
	private final LocalDateTime asmodianMinCreationTime = LocalDateTime.of(2016, Month.JANUARY, 17, 2, 0, 0);
	private final LocalDateTime asmodianMaxCreationTime = LocalDateTime.of(2016, Month.JULY, 1, 0, 0, 0);
	private final FactionPackDAO dao = DAOManager.getDAO(FactionPackDAO.class);
	private final FastTable<RewardItem> rewards = new FastTable<>();

	public static FactionPackService getInstance() {
		return INSTANCE;
	}

	private FactionPackService() {
		rewards.add(new RewardItem(186000236, 230)); // Blood Mark
		rewards.add(new RewardItem(162000124, 250)); // Superior Restoration Serum
		rewards.add(new RewardItem(162000023, 100)); // Greater Healing Potion
		rewards.add(new RewardItem(186000137, 1500)); // Courage Insignia
		rewards.add(new RewardItem(186000130, 3500)); // Crucible Insignia
		rewards.add(new RewardItem(160010121, 10)); // Inquin Bonbon
		rewards.add(new RewardItem(160010122, 10)); // Inquin Bonbon
	}

	public void addPlayerCustomReward(Player player) {
		if (rewards == null || rewards.isEmpty())
			return;
		if (player.getLevel() != 65)
			return;
		if (player.getCommonData().getMailboxLetters() + rewards.size() > 100)
			return;
		if (player.getRace() == Race.ASMODIANS)
			sendRewards(player, asmodianMinCreationTime, asmodianMaxCreationTime);
		else
			sendRewards(player, elyosMinCreationTime, elyosMaxCreationTime);
	}

	private void sendRewards(Player player, LocalDateTime minCreationTime, LocalDateTime maxCreationTime) {
		Timestamp playerCreationDate = player.getPlayerAccount().getPlayerAccountData(player.getObjectId()).getCreationDate();
		LocalDateTime creationTime = ServerTime.atDate(playerCreationDate).toLocalDateTime();
		if (creationTime.isBefore(minCreationTime))
			return;
		if (creationTime.isAfter(maxCreationTime))
			return;
		int accountId = player.getPlayerAccount().getId();
		if (dao.loadReceivingPlayer(accountId) > 0)
			return;
		if (!dao.storeReceivingPlayer(accountId, player.getObjectId()))
			return;
		for (RewardItem e : rewards) {
			ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(e.getId());
			if (template != null && template.getRace() == player.getOppositeRace())
				continue;
			SystemMailService.getInstance().sendMail("Beyond Aion",
				player.getName(), "Faction Pack", "Greetings Daeva!\n\n"
					+ "In gratitude for your decision to join the Elyos faction we prepared an additional item pack.\n\n" + "Enjoy your stay on Beyond Aion!",
				e.getId(), e.getCount(), 0, LetterType.EXPRESS);
		}
	}
}
