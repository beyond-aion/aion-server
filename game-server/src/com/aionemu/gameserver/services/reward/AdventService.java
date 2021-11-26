package com.aionemu.gameserver.services.reward;

import java.awt.*;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.List;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.AdventDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.survey.CustomSurveyItem;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Nathan, Estrayl, Neon, Sykra
 */
public class AdventService {

	private static final AdventService instance = new AdventService();
	private Map<Integer, List<CustomSurveyItem>> rewards = new HashMap<>();

	private AdventService() {
		for (int i = 1; i <= 25; i++)
			rewards.put(i, new ArrayList<>());
		initMaps();
	}

	private void initMaps() {
		// [Emotion Card] Snowman
		rewards.get(1).add(new CustomSurveyItem(169600206, 1));
		// Epsilon Enchantment Stone
		rewards.get(2).add(new CustomSurveyItem(166000195, 10));
		// Major Danuar Relic
		rewards.get(3).add(new CustomSurveyItem(186000247, 5));
		// [Event] Solorius Cookie
		rewards.get(4).add(new CustomSurveyItem(160010201, 25));
		// Honorable Conqueror's Mark
		rewards.get(5).add(new CustomSurveyItem(186000399, 75));
		// Solorius Furniture Set Box
		rewards.get(6).add(new CustomSurveyItem(188051879, 1));
		// [Event] Amplification Stone
		rewards.get(6).add(new CustomSurveyItem(166500005, 5));
		// Gathering Boost Charm II - 100%
		rewards.get(7).add(new CustomSurveyItem(169620082, 2));
		// [Event] Silver Box
		rewards.get(8).add(new CustomSurveyItem(188051709, 1));
		// [Event] Omega Enchantment Stone
		rewards.get(9).add(new CustomSurveyItem(166020003, 10));
		// [Event] Level 70 Composite Manastone Bundle
		rewards.get(10).add(new CustomSurveyItem(188053610, 5));
		// [Event] Solorius Coin
		rewards.get(11).add(new CustomSurveyItem(186000177, 15));
		// Ancient Coin
		rewards.get(12).add(new CustomSurveyItem(186000237, 1000));
		// [Event] Mithril Medal Box
		rewards.get(12).add(new CustomSurveyItem(188053667, 3));
		// Illusion Godstone Bundle
		rewards.get(13).add(new CustomSurveyItem(188053614, 1));
		// Ahserion's Flight Ancient Manastone Bundle
		rewards.get(14).add(new CustomSurveyItem(188053113, 3));
		// Blood Mark
		rewards.get(15).add(new CustomSurveyItem(186000236, 100));
		// Solorius Furniture Set Box
		rewards.get(16).add(new CustomSurveyItem(188051879, 1));
		// 12 Solorius Inquin Form Candy (ELYOS)
		rewards.get(17).add(new CustomSurveyItem(188051297, 1));
		// 12 Solorius Inquin Form Candy (ASMODIAN)
		rewards.get(17).add(new CustomSurveyItem(188051298, 1));
		// Tea of Repose - 100% Recovery
		rewards.get(18).add(new CustomSurveyItem(162001057, 10));
		// Daeva's Respite Coin
		rewards.get(19).add(new CustomSurveyItem(186000409, 100));
		// [Event] Drana Coffee
		rewards.get(19).add(new CustomSurveyItem(164002167, 10));
		// Enduring Mythic Weapon Tuning Scroll
		rewards.get(20).add(new CustomSurveyItem(166200013, 3));
		// [Event] Ceramium Medal Box
		rewards.get(21).add(new CustomSurveyItem(188053666, 3));
		// Honorable Elim's Idian Bundle
		rewards.get(22).add(new CustomSurveyItem(188053618, 1));
		// Solorius Garden Tree
		rewards.get(23).add(new CustomSurveyItem(170390116, 1));
		// [Event] Tempering Solution
		rewards.get(23).add(new CustomSurveyItem(166030007, 5));
		// Antriksha's Equipment Chest
		rewards.get(24).add(new CustomSurveyItem(188053109, 1));
		// Shugo Icebox
		rewards.get(24).add(new CustomSurveyItem(188052936, 1));
		// [Event] Santarinerk's Snowman Cabinet
		rewards.get(24).add(new CustomSurveyItem(170100037, 1));
	}

	public void onLogin(Player player) {
		ZonedDateTime now = ServerTime.now();
		int day = now.getDayOfMonth();
		if (now.getMonth() != Month.DECEMBER)
			return;
		if (!rewards.containsKey(day) || rewards.get(day).isEmpty())
			return;
		if (DAOManager.getDAO(AdventDAO.class).getLastReceivedDay(player) < day)
			PacketSendUtility.sendMessage(player,
				"You can open your advent calendar door for today!" + "\nType in .advent to redeem todays reward on this character.\n"
					+ ChatUtil.color("ATTENTION:", Color.ORANGE) + " Only one character per account can receive this reward!");
	}

	public void redeemReward(Player player) {
		ZonedDateTime now = ServerTime.now();
		int day = now.getDayOfMonth();
		List<CustomSurveyItem> todaysRewards = rewards.get(day);

		if (now.getMonth() != Month.DECEMBER || todaysRewards == null || todaysRewards.isEmpty()) {
			PacketSendUtility.sendMessage(player, "There is no advent calendar door for today.");
			return;
		}

		if (DAOManager.getDAO(AdventDAO.class).getLastReceivedDay(player) >= day) {
			PacketSendUtility.sendMessage(player, "You have already opened todays advent calendar door on this account.");
			return;
		}

		if (player.getMailbox().size() + calculateMailCount(player, todaysRewards) > 100) {
			PacketSendUtility.sendMessage(player, "You have not enough room in your mailbox.");
			return;
		}

		if (!DAOManager.getDAO(AdventDAO.class).storeLastReceivedDay(player, day)) {
			PacketSendUtility.sendMessage(player, "Sorry. Some shugo broke our database, please report this in our bugtracker :(");
			return;
		}

		for (CustomSurveyItem item : todaysRewards) {
			ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(item.getId());
			if (template != null && template.getRace() == player.getOppositeRace())
				continue;
			long maxStackCount = template.getMaxStackCount();
			if (item.getCount() <= maxStackCount) {
				sendRewardMail(player, item.getId(), item.getCount(), day);
				continue;
			}
			int remainingItemCount = item.getCount();
			while (remainingItemCount > maxStackCount) {
				remainingItemCount -= maxStackCount;
				sendRewardMail(player, item.getId(), (int) maxStackCount, day);
			}
			sendRewardMail(player, item.getId(), remainingItemCount, day);
		}
	}

	private int calculateMailCount(Player player, List<CustomSurveyItem> rewards) {
		int expectedMailCount = 0;
		for (CustomSurveyItem reward : rewards) {
			int itemId = reward.getId();
			ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
			if (itemTemplate == null || itemTemplate.getRace() == player.getOppositeRace())
				continue;
			if (reward.getCount() <= itemTemplate.getMaxStackCount()) {
				expectedMailCount++;
				continue;
			}
			long maxStackCount = itemTemplate.getMaxStackCount();
			int remainingItemCount = reward.getCount();
			while (remainingItemCount >= maxStackCount) {
				expectedMailCount++;
				remainingItemCount -= maxStackCount;
			}
		}
		return expectedMailCount;
	}

	private void sendRewardMail(Player player, int itemId, int count, int day) {
		SystemMailService.sendMail("Beyond Aion", player.getName(), "Advent Calendar",
			"Greetings Daeva!\n\nToday is December " + day
				+ " and you know what that means! Another day, another advent calendar door.\n\nWe hope you can use this well~\n\n-Beyond Aion",
			itemId, count, 0, LetterType.EXPRESS);
	}

	public void showTodaysReward(Player player) {
		ZonedDateTime now = ServerTime.now();
		int day = now.getDayOfMonth();
		List<CustomSurveyItem> todaysRewards = rewards.get(day);
		if (now.getMonth() != Month.DECEMBER || todaysRewards == null || todaysRewards.isEmpty()) {
			PacketSendUtility.sendMessage(player, "There is no advent calendar door for today.");
			return;
		}

		StringBuilder sb = new StringBuilder("Todays advent calendar reward(s):\n");

		for (Iterator<CustomSurveyItem> iter = todaysRewards.iterator(); iter.hasNext();) {
			int id = iter.next().getId();
			ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(id);
			if (template != null && template.getRace() == player.getOppositeRace())
				continue;
			sb.append(ChatUtil.item(id)).append(iter.hasNext() ? ", " : "");
		}
		PacketSendUtility.sendMessage(player, sb.toString());
	}

	public static AdventService getInstance() {
		return instance;
	}
}
