package playercommands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.custom.instance.CustomInstanceService;
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
 * @author Neon
 */
public class Trade extends PlayerCommand {

	private static final List<TradeItem> tradeList = new ArrayList<>();
	private static final int TRADE_CURRENCY_ITEM_ID = 186000344;

	public Trade() {
		super("trade", "Exchange your " + ChatUtil.item(TRADE_CURRENCY_ITEM_ID) + " for various rewards.");

		// @formatter:off
		setSyntaxInfo(
			" - Shows all buyable items.",
			"<item link|ID> [count] - Buys the respective item (default: 1, optional: number of items to buy)."
		);
		// @formatter:on
		initRewards();
	}

	private void initRewards() {
		addReward(CustomInstanceService.REWARD_COIN_ID, 8, "see " + ChatUtil.color(".buy", Color.WHITE) + " for what you can buy with it"); // Prestige Coin
		addReward(186000236, 20); // Blood Mark
		addReward(166200010, 600); // Mythic Armor Tuning Scroll
		addReward(166200009, 800); // Mythic Weapon Tuning Scroll
		addReward(169405391, 3000); // Upgraded Distorted Sword of the Fierce Battle Prototype
		addReward(169405392, 3000); // Upgraded Distorted Mace of the Fierce Battle Prototype
		addReward(169405393, 3000); // Upgraded Distorted Dagger of the Fierce Battle Prototype
		addReward(169405394, 3000); // Upgraded Distorted Greatsword of the Fierce Battle Prototype
		addReward(169405395, 3000); // Upgraded Distorted Polearm of the Fierce Battle Prototype
		addReward(169405396, 3000); // Upgraded Distorted Staff of the Fierce Battle Prototype
	}

	@Override
	protected void execute(Player player, String... params) {
		if (params.length < 1)
			sendInfo(player, createRewardInfo());
		else
			buyItem(player, params[0], params.length >= 2 ? params[1] : null);
	}

	private String createRewardInfo() {
		String rewardInfo = "Cost:\n";
		for (TradeItem tradeItem : tradeList) {
			String cost = String.valueOf(tradeItem.getCost());
			int digitsToPad = 4 - cost.length();
			if (digitsToPad > 0)
				cost = StringUtils.repeat(' ', digitsToPad * 2) + cost;
			rewardInfo += cost + "   ";
			rewardInfo += ChatUtil.item(tradeItem.getId()) + " " + tradeItem.getDescription() + "\n";
		}
		rewardInfo += "Type " + ChatUtil.color(getAliasWithPrefix() + " <item link|ID> <amount>", Color.WHITE) + " to buy the specified item (amount is optional).\n";
		rewardInfo += "Example: " + ChatUtil.color(getAliasWithPrefix(), Color.WHITE) + " " + ChatUtil.item(CustomInstanceService.REWARD_COIN_ID)
			+ ChatUtil.color(" 1000", Color.WHITE) + " to buy 1000 " + ChatUtil.itemName(CustomInstanceService.REWARD_COIN_ID)+".";
		return rewardInfo;
	}

	private void buyItem(Player player, String itemLink, String amountStr) {
		// redeem item
		int itemId = ChatUtil.getItemId(itemLink);
		if (itemId == 0 || DataManager.ITEM_DATA.getItemTemplate(itemId) == null) {
			sendInfo(player, "\"" + itemLink + "\" is not a valid item (must be an item link or ID).");
			return;
		}

		TradeItem item = tradeList.stream().filter(tradeItem -> tradeItem.getId() == itemId).findFirst().orElse(null);
		if (item == null) {
			sendInfo(player, ChatUtil.item(itemId) + " is not contained in the rewards.");
			return;
		}
		long amount = amountStr != null ? Integer.parseInt(amountStr) : 1;
		long totalCost = amount * item.getCost();
		long rewardCoins = 0;
		for (Item i : player.getInventory().getItemsByItemId(TRADE_CURRENCY_ITEM_ID))
			rewardCoins += i.getItemCount();

		if (totalCost > rewardCoins) {
			sendInfo(player, "You need " + totalCost + " to buy " + amount + "x " + ChatUtil.item(item.getId()) + ".");
			return;
		}

		RequestResponseHandler<Creature> handler = new RequestResponseHandler<Creature>(null) {

			@Override
			public void acceptRequest(Creature requester, Player responder) {
				if (player.getInventory().decreaseByItemId(TRADE_CURRENCY_ITEM_ID, totalCost)) {
					sendInfo(player, "You have spent " + totalCost + ".");
					ItemService.addItem(player, item.getId(), amount, true,
						new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_CASH_ITEM));
				}
			}
		};
		if (player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_AIONJEWEL_SHOP_BUY_CONFIRM, handler))
			PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_AIONJEWEL_SHOP_BUY_CONFIRM, 0, 0, totalCost,
				DataManager.ITEM_DATA.getItemTemplate(TRADE_CURRENCY_ITEM_ID).getL10n()));
	}

	private void addReward(int itemID, int cost) {
		addReward(itemID, cost, "");
	}

	private void addReward(int itemId, int cost, String description) {
		tradeList.add(new TradeItem(itemId, cost, description));
	}

	private static class TradeItem {

		private final int id;
		private final long cost;
		private final String description;

		private TradeItem(int id, long cost, String desc) {
			this.id = id;
			this.cost = cost;
			this.description = desc;
		}

		public int getId() {
			return id;
		}

		public long getCost() {
			return cost;
		}

		public String getDescription() {
			return description;
		}

	}
}
