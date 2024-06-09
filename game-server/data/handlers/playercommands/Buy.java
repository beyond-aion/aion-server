package playercommands;

import static com.aionemu.gameserver.custom.instance.CustomInstanceService.REWARD_COIN_ID;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.dataholders.DataManager;
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
		super("buy", "Exchange your " + ChatUtil.item(REWARD_COIN_ID) + " for various rewards.");

		// @formatter:off
		setSyntaxInfo(
			" - Shows all buyable rewards.",
			"<item link|ID> - Buys the respective item."
		);
		// @formatter:on
		initRewards();
	}

	private void initRewards() {
		addReward(166000194, 5, 40); // Delta Enchantment Stone
		addReward(166000195, 5, 50); // Epsilon Enchantment Stone
		addReward(188051516, 1, 100); // Smart Greater Scroll Bundle
		addReward(162000107, 30, 100); // Saam King's Herb
		addReward(162000124, 50, 100); // Superior Recovery Serum
		addReward(188051868, 1, 50); // [Event] Strongest Inquin Form Candy Pouch
		addReward(188053610, 3, 150); // [Event] Level 70 Composite Manastone Bundle
		addReward(188053618, 1, 200); // Honorable Elim's Idian Bundle
		addReward(166500002, 2, 50); // Amplification Stone
		addReward(166030005, 2, 80); // Tempering Solution
		addReward(188053295, 1, 200); // Empyrean Plume Chest
		addReward(188950015, 1, 400); // Special Courier Pass (Eternal/Lv. 61-65)
		addReward(188950019, 1, 500); // Special Courier Pass (Mythic/Lv. 61-65)
		addReward(165020015, 1, 1000); // Armor Wrapping Scroll (Eternal/Lv. 65 and lower)
		addReward(165020014, 1, 1150); // Weapon Wrapping Scroll (Eternal/Lv. 65 and lower)
		addReward(165020021, 1, 1350); // Noble Armor Wrapping Scroll (Mythic/Lv. 65 and lower)
		addReward(165020020, 1, 1500); // Noble Weapon Wrapping Scroll (Mythic/Lv. 65 and lower)
		addReward(190020175, 1, 1500); // Tahabata Egg
		addReward(187060103, 1, 1300); // Prestige Wings
		addReward(169610342, 1, 1000); // [Title] Forgotten Conqueror
		addReward(190100051, 1, 1200); // Flying Pagati
		addReward(188053109, 1, 2500); // Ahserion's Equipment Chest
	}

	@Override
	protected void execute(Player player, String... params) {
		if (params.length == 0)
			showRewards(player);
		else if (params.length == 1)
			buyItem(player, params[0]);
		else
			sendInfo(player);
	}

	private void showRewards(Player player) {
		sendInfo(player, "Cost:");
		for (Entry<Integer, Map<String, Integer>> idMap : rewards.entrySet()) {
			String itemString;
			String cost = String.valueOf(idMap.getValue().get("cost"));
			int amount = idMap.getValue().get("amount");
			int digitsToPad = 4 - cost.length();
			if (digitsToPad > 0)
				cost = StringUtils.repeat(' ', digitsToPad * 2) + cost;

			if (amount == 1)
				itemString = cost + "   ";
			else
				itemString = cost + "   " + amount + "x ";

			itemString += ChatUtil.item(idMap.getKey());
			sendInfo(player, itemString);
		}
		sendInfo(player, "To buy:");
		sendInfo(player, "1. Right-click an item from the list to your Memo Pad.");
		sendInfo(player, "2. Type " + ChatUtil.color(getAliasWithPrefix(), Color.WHITE) + " in your chat field (with space).");
		sendInfo(player, "3. (Ctrl + Right-click) the item from your Memo Pad.");
	}

	private void buyItem(Player player, String itemLink) {
		// redeem item
		int itemId = ChatUtil.getItemId(itemLink);
		if (DataManager.ITEM_DATA.getItemTemplate(itemId) == null) {
			sendInfo(player, "\"" + itemLink + "\" is not a valid item (must be an item link or ID).");
			return;
		}

		if (!rewards.containsKey(itemId)) {
			sendInfo(player, ChatUtil.item(itemId) + " is not contained in the rewards.");
			return;
		}
		int cost = rewards.get(itemId).get("cost");
		long rewardCoins = 0;
		for (Item i : player.getInventory().getItemsByItemId(REWARD_COIN_ID))
			rewardCoins += i.getItemCount();

		if (cost > rewardCoins) {
			sendInfo(player, "You need " + cost + " to buy " + ChatUtil.item(itemId) + ".");
			return;
		}

		RequestResponseHandler<Creature> handler = new RequestResponseHandler<Creature>(null) {

			@Override
			public void acceptRequest(Creature requester, Player responder) {
				if (player.getInventory().decreaseByItemId(REWARD_COIN_ID, cost)) {
					sendInfo(player, "You have spent " + cost + ".");
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
