package playercommands;

import static com.aionemu.gameserver.custom.instance.CustomInstanceService.REWARD_COIN_ID;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Estrayl
 */
public class Buy extends PlayerCommand {

	private static final Map<Integer, Map<String, Integer>> rewards = new LinkedHashMap<>();

	public Buy() {
		super("buy", "Display or redeem loyalty rewards.");

		// @formatter:off
		setSyntaxInfo(
			"list - Shows all buyable rewards.",
			"<item link|ID> - Buys the respective item."
		);
		// @formatter:on
		initRewards();
	}

	private void initRewards() {
		addReward(166000194, 5, 50); // Delta Enchantment Stone
		addReward(166000195, 5, 70); // Epsilon Enchantment Stone
		addReward(188051516, 1, 150); // Smart Greater Scroll Bundle
		addReward(162000107, 30, 150); // Saam King's Herb
		addReward(162000124, 50, 150); // Superior Recovery Serum
		addReward(188051868, 1, 60); // [Event] Strongest Inquin Form Candy Pouch
		addReward(188053610, 3, 200); // [Event] Level 70 Composite Manastone Bundle
		addReward(188053618, 1, 300); // Honorable Elim's Idian Bundle
		addReward(166500002, 2, 60); // Amplification Stone
		addReward(166030005, 2, 100); // Tempering Solution
		addReward(188053295, 1, 300); // Empyrean Plume Chest
		addReward(188950015, 1, 800); // Special Courier Pass (Eternal/Lv. 61-65)
		addReward(188950019, 1, 1000); // Special Courier Pass (Mythic/Lv. 61-65)
		addReward(165020015, 1, 2000); // Armor Wrapping Scroll (Eternal/Lv. 65 and lower)
		addReward(165020014, 1, 2250); // Weapon Wrapping Scroll (Eternal/Lv. 65 and lower)
		addReward(165020021, 1, 2750); // Noble Armor Wrapping Scroll (Mythic/Lv. 65 and lower)
		addReward(165020020, 1, 3000); // Noble Weapon Wrapping Scroll (Mythic/Lv. 65 and lower)
		addReward(190020175, 1, 1500); // Tahabata Egg
		addReward(187060103, 1, 1300); // Prestige Wings
		addReward(169610342, 1, 1000); // [Title] Forgotten Conqueror
		addReward(190100051, 1, 1200); // Flying Pagati
		addReward(188053109, 1, 2500); // Ahserion's Equipment Chest
	}

	@Override
	protected void execute(Player player, String... params) {
		if (params.length != 1)
			sendInfo(player);
		else if ("list".equalsIgnoreCase(params[0]))
			showRewards(player);
		else
			buyItem(player, params[0]);
	}

	private void showRewards(Player player) {
		PacketSendUtility.sendMessage(player, "Cost:", ChatType.YELLOW);
		for (Entry<Integer, Map<String, Integer>> idMap : rewards.entrySet()) {
			String itemString = "";
			int cost = idMap.getValue().get("cost");
			int amount = idMap.getValue().get("amount");
			if (cost < 1000) // indentation
				itemString = "  " + itemString;
			if (cost < 100)
				itemString = "  " + itemString;
			if (cost < 10)
				itemString = "  " + itemString;

			if (amount == 1)
				itemString = itemString + cost + "   ";
			else
				itemString = itemString + cost + "   " + amount + "x ";

			itemString += "[item: " + idMap.getKey() + "]"; // ID
			PacketSendUtility.sendMessage(player, itemString, ChatType.YELLOW);
		}
		PacketSendUtility.sendMessage(player, "To buy:", ChatType.YELLOW);
		PacketSendUtility.sendMessage(player, "1. Right-click an item from the list to your Memo Pad.", ChatType.YELLOW);
		PacketSendUtility.sendMessage(player, "2. Type \"." + getAlias() + "\" in your chat field (with space).", ChatType.YELLOW);
		PacketSendUtility.sendMessage(player, "3. (Ctrl + Right-click) the item from your Memo Pad.", ChatType.YELLOW);
	}

	private void buyItem(Player player, String itemLink) {
		// redeem item
		int itemId = ChatUtil.getItemId(itemLink);
		if (DataManager.ITEM_DATA.getItemTemplate(itemId) == null) {
			PacketSendUtility.sendMessage(player, itemLink + " is not a valid item.", ChatType.YELLOW);
			return;
		}

		if (!rewards.containsKey(itemId)) {
			PacketSendUtility.sendMessage(player, ChatUtil.item(itemId) + " is not contained in the rewards.", ChatType.YELLOW);
			return;
		}
		int cost = rewards.get(itemId).get("cost");
		int rewardCoins = 0;
		for (Item i : player.getInventory().getItemsByItemId(REWARD_COIN_ID))
			rewardCoins += i.getItemCount();

		if (cost > rewardCoins) {
			PacketSendUtility.sendMessage(player, "You need " + cost + " to buy " + "[item: " + itemId + "].", ChatType.YELLOW);
			return;
		}

		RequestResponseHandler<Creature> handler = new RequestResponseHandler<Creature>(null) {

			@Override
			public void acceptRequest(Creature requester, Player responder) {
				if (player.getInventory().decreaseByItemId(REWARD_COIN_ID, cost)) {
					PacketSendUtility.sendMessage(player, "You have spent " + cost + ".", ChatType.YELLOW);
					ItemService.addItem(player, itemId, rewards.get(itemId).get("amount"), true,
						new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_CASH_ITEM));
				}
			}
		};
		if (player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_AIONJEWEL_SHOP_BUY_CONFIRM, handler))
			PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_AIONJEWEL_SHOP_BUY_CONFIRM, 0, 0, cost,
				DataManager.ITEM_DATA.getItemTemplate(REWARD_COIN_ID).getL10n()));
	}

	private void addReward(int itemID, int amount, int cost) {
		Map<String, Integer> val = new LinkedHashMap<>();
		val.put("cost", cost);
		val.put("amount", amount);
		rewards.put(itemID, val);
	}
}
