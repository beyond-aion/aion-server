package com.aionemu.gameserver.services.reward;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.VeteranRewardDAO;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.time.ServerTime;

import javolution.util.FastTable;

/**
 * @author Neon
 */
public final class VeteranRewardService {

	private static final List<List<RewardItem>> rewards = new FastTable<>();

	static {
		for (int i = 0; i < 18; i++)
			rewards.add(new FastTable<>());
		// month 1
		rewards.get(0).add(new RewardItem(169630007, 1)); // [Expand Card] Expand Cube Ticket (lvl 4)
		rewards.get(0).add(new RewardItem(169620094, 1)); // Crafting Boost Charm III - 100%
		rewards.get(0).add(new RewardItem(161001001, 5)); // Revival Stone
		rewards.get(0).add(new RewardItem(162000124, 50)); // Superior Recovery Serum

		// month 2
		rewards.get(1).add(new RewardItem(190020075, 1)); // Flash Bogel Egg
		rewards.get(1).add(new RewardItem(169600064, 1)); // [Emotion Card] Playing Dead
		rewards.get(1).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(1).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 3
		rewards.get(2).add(new RewardItem(125040038, 1)); // Devil Horns
		rewards.get(2).add(new RewardItem(186000199, 100)); // Legion Coin
		rewards.get(2).add(new RewardItem(166000195, 5)); // Epsilon Enchantment Stone
		rewards.get(2).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%

		// month 4
		rewards.get(3).add(new RewardItem(169640006, 1)); // [Expand Card] Expand Warehouse Ticket (lvl 4)
		rewards.get(3).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(3).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		rewards.get(3).add(new RewardItem(162000107, 50)); // Saam King's Herbs

		// month 5
		rewards.get(4).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(4).add(new RewardItem(169600103, 1)); // [Emotion Card] Diving
		rewards.get(4).add(new RewardItem(161001001, 5)); // Revival Stone
		rewards.get(4).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(4).add(new RewardItem(186000051, 5)); // Major Ancient Crown

		// month 6
		rewards.get(5).add(new RewardItem(187000057, 1)); // Kahrun's Wing
		rewards.get(5).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		rewards.get(5).add(new RewardItem(164002264, 25)); // Flame Pillar Firecracker
		rewards.get(5).add(new RewardItem(169670000, 1)); // Name Change Ticket

		// month 7
		rewards.get(6).add(new RewardItem(169630007, 1)); // [Expand Card] Expand Cube Ticket (lvl 4)
		rewards.get(6).add(new RewardItem(169600065, 1)); // [Emotion Card] Sing
		rewards.get(6).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%
		rewards.get(6).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(6).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 8
		rewards.get(7).add(new RewardItem(190000048, 1)); // Golden Nyanco Egg
		rewards.get(7).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(7).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		rewards.get(7).add(new RewardItem(186000199, 100)); // Legion Coin

		// month 9
		rewards.get(8).add(new RewardItem(166000195, 5)); // Epsilon Enchantment Stone
		rewards.get(8).add(new RewardItem(169600087, 1)); // [Emotion Card] 'Bad Girl' Dance
		rewards.get(8).add(new RewardItem(188052761, 5)); // [Event] Bonus Entry Scroll Bundle
		rewards.get(8).add(new RewardItem(162000107, 50)); // Saam King's Herbs

		// month 10
		rewards.get(9).add(new RewardItem(169640006, 1)); // [Expand Card] Expand Warehouse Ticket (lvl 4)
		rewards.get(9).add(new RewardItem(169650007, 1)); // [Event] Plastic Surgery Ticket
		rewards.get(9).add(new RewardItem(186000051, 5)); // Major Ancient Crown
		rewards.get(9).add(new RewardItem(161001001, 5)); // Revival Stone

		// month 11
		rewards.get(10).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(10).add(new RewardItem(164002284, 25)); // [Event] Ornate Firecrackers
		rewards.get(10).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(10).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%

		// month 12
		rewards.get(11).add(new RewardItem(190100049, 1)); // Flying Crestlich
		rewards.get(11).add(new RewardItem(169600062, 1)); // [Emotion Card] Play Harp
		rewards.get(11).add(new RewardItem(169610343, 1)); // [Title] Forgotten Hero
		rewards.get(11).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone

		// month 13
		rewards.get(12).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(12).add(new RewardItem(169660003, 1)); // [Event] Gender Switch Ticket
		rewards.get(12).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(12).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 14
		rewards.get(13).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(13).add(new RewardItem(169600063, 1)); // [Emotion Card] Play the Saxophone
		rewards.get(13).add(new RewardItem(188052761, 5)); // [Event] Bonus Entry Scroll Bundle
		rewards.get(13).add(new RewardItem(162000107, 50)); // Saam King's Herbs

		// month 15
		rewards.get(14).add(new RewardItem(110900876, 1)); // Nyerkcarrier
		rewards.get(14).add(new RewardItem(190020156, 1)); // [Event] Medalist Shugo Egg
		rewards.get(14).add(new RewardItem(166000195, 5)); // Epsilon Enchantment Stone
		rewards.get(14).add(new RewardItem(161001001, 5)); // Revival Stone

		// month 16
		rewards.get(15).add(new RewardItem(188053526, 5)); // [Event] Aion's Steel Form Candy Box
		rewards.get(15).add(new RewardItem(169600060, 1)); // [Emotion Card] Play the Drum
		rewards.get(15).add(new RewardItem(188052719, 5)); // [Event] Dye Bundle
		rewards.get(15).add(new RewardItem(186000051, 5)); // Major Ancient Crown
		rewards.get(15).add(new RewardItem(169620072, 3)); // AP Boost Charm II - 30%

		// month 17
		rewards.get(16).add(new RewardItem(166030007, 5)); // [Event] Tempering Solution
		rewards.get(16).add(new RewardItem(164002284, 25)); // [Event] Ornate Firecrackers
		rewards.get(16).add(new RewardItem(186000242, 15)); // Ceramium Medal
		rewards.get(16).add(new RewardItem(162000137, 25)); // Sublime Life Serum
		rewards.get(16).add(new RewardItem(162000139, 25)); // Sublime Mana Serum

		// month 18
		rewards.get(17).add(new RewardItem(187060162, 1)); // Wings of Agony
		rewards.get(17).add(new RewardItem(168310018, 1)); // Major Blessed Augment: Level 2
		rewards.get(17).add(new RewardItem(166020003, 5)); // [Event] Omega Enchantment Stone
		rewards.get(17).add(new RewardItem(162000107, 50)); // Saam King's Herbs
	}

	/**
	 * Prevent instantiation
	 */
	private VeteranRewardService() {
	}

	public static VeteranRewardService getInstance() {
		return SingletonHolder.instance;
	}

	public void tryReward(Player player) {
		if (player.getLevel() != 65)
			return;

		Account playerAcc = player.getPlayerAccount();
		ZonedDateTime now = ServerTime.now();
		ZonedDateTime charCreationTime = ServerTime.atDate(playerAcc.getPlayerAccountData(player.getObjectId()).getCreationDate());
		if (ChronoUnit.MONTHS.between(charCreationTime, now) < 1) // return if char is younger than a month
			return;

		ZonedDateTime accCreationTime = ServerTime.ofEpochMilli(playerAcc.getCreationDate());
		long months = ChronoUnit.MONTHS.between(accCreationTime, now);
		if (months < 1) // return if account is younger than a month
			return;

		int monthsToReceive = (int) Math.min(months, rewards.size());
		int receivedMonths = DAOManager.getDAO(VeteranRewardDAO.class).loadReceivedMonths(player); // -1 means error
		if (receivedMonths < 0 || receivedMonths >= monthsToReceive)
			return;

		if (DAOManager.getDAO(VeteranRewardDAO.class).storeReceivedMonths(player, monthsToReceive))
			for (int i = receivedMonths; i < monthsToReceive; i++) {
				List<RewardItem> items = rewards.get(i);
				if (player.getMailbox().getLetters().size() >= 100) { // abort on mailbox overflow and save the correct month
					DAOManager.getDAO(VeteranRewardDAO.class).storeReceivedMonths(player, i);
					return;
				}
				for (RewardItem item : items)
					SystemMailService.getInstance().sendMail("Beyond Aion", player.getName(), "Veteran Reward",
						"Greetings Daeva!\n\nIt has been over " + (i == 0 ? "a month" : (i + 1) + " months")
							+ " now, since you joined us.\nWe send you this and hope you stay with us even longer :)\n\n~ Beyond Aion",
						item.getId(), item.getCount(), 0, LetterType.BLACKCLOUD);
			}
	}

	private static final class SingletonHolder {

		static final VeteranRewardService instance = new VeteranRewardService();
	}
}
