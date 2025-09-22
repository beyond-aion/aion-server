package com.aionemu.gameserver.services.reward;

import java.awt.Color;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.*;

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
	private final Map<Integer, List<CustomSurveyItem>> rewards = new HashMap<>();

	private AdventService() {
		for (int i = 1; i <= 25; i++)
			rewards.put(i, new ArrayList<>());
		initMaps();
	}

	private void initMaps() {
		// Solorius Glasses
		rewards.get(1).add(new CustomSurveyItem(125045107, 1));
		// [Event] Solorius Cookie
		rewards.get(2).add(new CustomSurveyItem(160010201, 25));
		// Gathering Boost Charm II - 100%
		rewards.get(3).add(new CustomSurveyItem(169620082, 1));
		// [Event] 12 Solorius Polar Bear Form Candy (ELYOS)
		rewards.get(4).add(new CustomSurveyItem(188051293, 1));
		// [Event] 12 Solorius Polar Bear Form Candy (ASMODIANS)
		rewards.get(4).add(new CustomSurveyItem(188051294, 1));
		// Tempering Solution
		rewards.get(5).add(new CustomSurveyItem(166030005, 5));
		// [Solorius] Display Furniture Pack
		rewards.get(6).add(new CustomSurveyItem(188051875, 1));
		// [Event] Drana Coffee
		rewards.get(7).add(new CustomSurveyItem(164002167, 25));
		// Amplification Stone
		rewards.get(8).add(new CustomSurveyItem(166500002, 5));
		// AP Boost Charm II - 30%
		rewards.get(9).add(new CustomSurveyItem(169620072, 2));
		// [Event] Solorius Coin
		rewards.get(10).add(new CustomSurveyItem(186000177, 15));
		// Blood Mark Box
		rewards.get(11).add(new CustomSurveyItem(188053636, 2));
		// Omega Enchantment Stone
		rewards.get(12).add(new CustomSurveyItem(166020000, 10));
		// Dashing Solorius Costume
		rewards.get(13).add(new CustomSurveyItem(110900933, 1));
		// Dashing Solorius Hat
		rewards.get(13).add(new CustomSurveyItem(125045555, 1));
		// Ceramium Medal
		rewards.get(14).add(new CustomSurveyItem(186000242, 5));
		// [Event] Level 60 Composite Manastone Bundle
		rewards.get(15).add(new CustomSurveyItem(188053609, 5));
		// Ahserion's Flight Ancient Manastone Bundle
		rewards.get(16).add(new CustomSurveyItem(188053113, 3));
		// [Solorius] Display Furniture Pack
		rewards.get(17).add(new CustomSurveyItem(188051875, 1));
		// Honorable Elim's Idian Bundle
		rewards.get(18).add(new CustomSurveyItem(188053618, 1));
		// Sublime Life Serum
		rewards.get(19).add(new CustomSurveyItem(162000137, 15));
		// Sublime Mana Serum
		rewards.get(19).add(new CustomSurveyItem(162000139, 15));
		// Sublime Wind Serum
		rewards.get(19).add(new CustomSurveyItem(162000141, 15));
		// Crafting Boost Charm III - 100%
		rewards.get(20).add(new CustomSurveyItem(169620094, 1));
		// Special Courier Pass (Mythic/Lv. 61-65)
		rewards.get(21).add(new CustomSurveyItem(188950019, 2));
		// Major Danuar Relic
		rewards.get(22).add(new CustomSurveyItem(186000247, 5));
		// [Event] Level 70 Composite Manastone Bundle
		rewards.get(23).add(new CustomSurveyItem(188053610, 3));
		// Ahserion's Equipment Box
		rewards.get(24).add(new CustomSurveyItem(188053109, 1));
		// Assured Greater Felicitous Socketing (Eternal)
		rewards.get(24).add(new CustomSurveyItem(166150018, 5));
	}

	public void onLogin(Player player) {
		ZonedDateTime now = ServerTime.now();
		int day = now.getDayOfMonth();
		if (now.getMonth() != Month.DECEMBER)
			return;
		if (!rewards.containsKey(day) || rewards.get(day).isEmpty())
			return;
		if (AdventDAO.getLastReceivedDay(player) < day)
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

		if (AdventDAO.getLastReceivedDay(player) >= day) {
			PacketSendUtility.sendMessage(player, "You have already opened todays advent calendar door on this account.");
			return;
		}

		if (player.getMailbox().size() + calculateMailCount(player, todaysRewards) > 100) {
			PacketSendUtility.sendMessage(player, "You have not enough room in your mailbox.");
			return;
		}

		if (!AdventDAO.storeLastReceivedDay(player, day)) {
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
			long remainingItemCount = item.getCount();
			while (remainingItemCount > maxStackCount) {
				remainingItemCount -= maxStackCount;
				sendRewardMail(player, item.getId(), Math.toIntExact(maxStackCount), day);
			}
			sendRewardMail(player, item.getId(), Math.toIntExact(remainingItemCount), day);
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
			long remainingItemCount = reward.getCount();
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
