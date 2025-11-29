package com.aionemu.gameserver.services.reward;

import java.awt.Color;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dao.AdventDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Nathan, Estrayl, Neon, Sykra
 */
public class AdventService {

	private static final AdventService instance = new AdventService();
	private final Map<Integer, List<RewardItem>> rewards = new HashMap<>();

	private AdventService() {
		addReward(1, 170190034, 1); // [Event] Solorius Cake
		addReward(2, 125040166, 1); // Solorius Hairpin/Top Hat
		addReward(3, 186000237, 1000); // Ancient Coin
		addReward(4, 188051879, 1); // Solorius Furniture Set Box
		addReward(5, 166100011, 600); // Greater Supplements (Mythic)
		addReward(6, 190020236, 1); // Mini Hyperion Egg (30 days)
		addReward(7, 188051297, 1); // 12 Solorius Inquin Form Candy (Elyos)
		addReward(7, 188051298, 1); // 12 Solorius Inquin Form Candy (Asmodians)
		addReward(8, 166020003, 10); // [Event] Omega Enchantment Stone
		addReward(9, 170390016, 1); // Solorius Garden Tree (Elyos)
		addReward(9, 170395016, 1); // Solorius Garden Tree (Asmodians)
		addReward(10, 160010201, 25); // [Event] Solorius Cookie
		addReward(11, 162001057, 5); // Tea of Repose - 100% Recovery
		addReward(12, 188050004, 5); // Red Solorius Stocking (Elyos)
		addReward(12, 188050007, 5); // Red Solorius Stocking (Asmodians)
		addReward(13, 110900665, 1); // Resplendent Jolly Coat
		addReward(14, 166030007, 5); // [Event] Tempering Solution
		addReward(15, 188054014, 5); // [Event] Lunahare Kisk Box
		addReward(16, 164002167, 25); // [Event] Drana Coffee
		addReward(17, 188051879, 1); // Solorius Furniture Set Box
		addReward(18, 188051299, 1); // 12 Solorius Tiger Form Candy (Elyos)
		addReward(18, 188051300, 1); // 12 Solorius Tiger Form Candy (Asmodians)
		addReward(19, 186000143, 250); // Kahrun's Symbol
		addReward(20, 162002018, 20); // [Event] Wormwood Dish
		addReward(21, 188050006, 5); // Green Solorius Stocking (Elyos)
		addReward(21, 188050009, 5); // Green Solorius Stocking (Asmodians)
		addReward(22, 166500005, 5); // [Event] Amplification Stone
		addReward(23, 160010201, 25); // [Event] Solorius Cookie
		addReward(24, 190020109, 1); // Solorinerk Egg
		addReward(24, 188053610, 5); // [Event] Level 70 Composite Manastone Bundle
		addReward(24, 166150019, 5); // Assured Greater Felicitous Socketing (Mythic)
	}

	private void addReward(int day, int itemId, long itemCount) {
		rewards.computeIfAbsent(day, _ -> new ArrayList<>()).add(new RewardItem(itemId, itemCount));
	}

	public void onLogin(Player player) {
		if (!EventsConfig.ENABLE_ADVENT_CALENDAR)
			return;
		LocalDate today = ServerTime.now().toLocalDate();
		if (!isAdventSeason(today))
			return;
		if (!ChatProcessor.getInstance().isCommandAllowed(player, "advent"))
			return;
		int day = today.getDayOfMonth();
		if (!rewards.containsKey(day) || rewards.get(day).isEmpty() || !AdventDAO.canReceiveReward(player, today))
			return;
		PacketSendUtility.sendMessage(player,
			"You can open your advent calendar door for today!" + "\nType in .advent to redeem todays reward on this character.\n"
				+ ChatUtil.color("ATTENTION:", Color.PINK) + " Only one character per account can receive this reward!");
	}

	public boolean isAdventSeason() {
		return isAdventSeason(ServerTime.now().toLocalDate());
	}

	private boolean isAdventSeason(LocalDate date) {
		return date.getMonth() == Month.DECEMBER && date.getDayOfMonth() <= 24;
	}

	public void redeemReward(Player player) {
		LocalDate today = ServerTime.now().toLocalDate();
		int day = today.getDayOfMonth();
		List<RewardItem> todaysRewards = rewards.get(day);

		if (!isAdventSeason(today) || todaysRewards == null || todaysRewards.isEmpty()) {
			PacketSendUtility.sendMessage(player, "There is no advent calendar door for today.");
			return;
		}

		if (!AdventDAO.canReceiveReward(player, today)) {
			PacketSendUtility.sendMessage(player, "You have already opened today's advent calendar door on this account.");
			return;
		}

		long regularCubeItems = todaysRewards.stream()
			.map(r -> DataManager.ITEM_DATA.getItemTemplate(r.getId()))
			.filter(r -> r.getExtraInventoryId() <= 0)
			.filter(r -> r.getRace() != player.getOppositeRace())
			.count();
		if (player.getInventory().getFreeSlots() < regularCubeItems) {
			PacketSendUtility.sendMessage(player, "You don't have enough free slots in your inventory.");
			return;
		}

		if (!AdventDAO.storeLastReceivedDay(player, today)) {
			PacketSendUtility.sendMessage(player, "Sorry. Some shugo broke our database, please report this in our bugtracker :(");
			return;
		}

		for (RewardItem item : todaysRewards) {
			if (DataManager.ITEM_DATA.getItemTemplate(item.getId()).getRace() == player.getOppositeRace())
				continue;
			ItemService.addItem(player, item.getId(), item.getCount(), true);
		}
	}

	public void showTodaysReward(Player player) {
		LocalDate today = ServerTime.now().toLocalDate();
		List<RewardItem> todaysRewards = rewards.get(today.getDayOfMonth());
		if (today.getMonth() != Month.DECEMBER || todaysRewards == null || todaysRewards.isEmpty()) {
			PacketSendUtility.sendMessage(player, "There is no advent calendar door for today.");
			return;
		}

		StringBuilder sb = new StringBuilder("Today's advent calendar reward(s):\n");

		for (Iterator<RewardItem> iter = todaysRewards.iterator(); iter.hasNext();) {
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
