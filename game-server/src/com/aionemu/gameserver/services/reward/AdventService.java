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
		// [Event] Siel's Solorius Beret
		rewards.get(1).add(new CustomSurveyItem(125040122, 1));
		// Daeva's Respite Coin
		rewards.get(2).add(new CustomSurveyItem(186000409, 100));
		// Major Ancient Crown
		rewards.get(3).add(new CustomSurveyItem(186000051, 10));
		// [Event] Omega Enchantment Stone
		rewards.get(4).add(new CustomSurveyItem(166020003, 10));
		// Solorius Furniture Set Box
		rewards.get(5).add(new CustomSurveyItem(188051879, 1));
		// Snowman Skin
		rewards.get(6).add(new CustomSurveyItem(110900680, 1));
		// Teasing Jefi Egg
		rewards.get(7).add(new CustomSurveyItem(190020030, 1));
		// Ancient Coin
		rewards.get(8).add(new CustomSurveyItem(186000237, 1000));
		// AP Boost Charm II - 30%
		rewards.get(9).add(new CustomSurveyItem(169620072, 1));
		// Green Solorius Stocking (ELYOS)
		rewards.get(10).add(new CustomSurveyItem(188050006, 5));
		// Green Solorius Stocking (ASMODIAN)
		rewards.get(10).add(new CustomSurveyItem(188050009, 5));
		// [Event] Amplification Stone
		rewards.get(11).add(new CustomSurveyItem(166500005, 10));
		// Solorius Tree
		rewards.get(12).add(new CustomSurveyItem(170030057, 1));
		// Red Solorius Stocking (ELYOS)
		rewards.get(13).add(new CustomSurveyItem(188050004, 5));
		// Red Solorius Stocking (ASMODIAN)
		rewards.get(13).add(new CustomSurveyItem(188050007, 5));
		// Ahserion's Flight Ancient Manastone Bundle
		rewards.get(14).add(new CustomSurveyItem(188053113, 3));
		// Blood Mark
		rewards.get(15).add(new CustomSurveyItem(186000236, 100));
		// +5 Snowball
		rewards.get(16).add(new CustomSurveyItem(188052990, 5));
		// 12 Solorius Inquin Form Candy (ELYOS)
		rewards.get(17).add(new CustomSurveyItem(188051297, 1));
		// 12 Solorius Inquin Form Candy (ASMODIAN)
		rewards.get(17).add(new CustomSurveyItem(188051298, 1));
		// [Event] Tempering Solution
		rewards.get(18).add(new CustomSurveyItem(166030007, 10));
		// [Event] Drana Coffee
		rewards.get(19).add(new CustomSurveyItem(164002167, 25));
		// Solorius Hearth
		rewards.get(20).add(new CustomSurveyItem(170150003, 1));
		// [Event] Ceramium Medal Box
		rewards.get(21).add(new CustomSurveyItem(188053666, 3));
		// Honorable Elim's Idian Bundle
		rewards.get(22).add(new CustomSurveyItem(188053618, 1));
		// [Event] Santarinerk's Snowman Cabinet
		rewards.get(23).add(new CustomSurveyItem(170100037, 1));
		// [Event] Brilliant Ice Prism
		rewards.get(24).add(new CustomSurveyItem(188052999, 1));
		// [Event] Level 70 Composite Manastone Bundle
		rewards.get(24).add(new CustomSurveyItem(188053610, 5));
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
