package playercommands;

import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Neon
 */
public class Easter extends PlayerCommand {

	private static final Logger log = LoggerFactory.getLogger(Easter.class);
	private static int neededItem = 186000175;
	private static int[][] rewards = { { 150, 0 }, { 350, 110900568 }, { 350, 125045476 }, { 750, 190020198 }, { 750, 190020060 }, { 1000, 170100036 } };
	private static int[][] randomItems = {
		{ 3, 166030007 }, // [Event] Tempering Solution
		{ 50, 162001034 }, // [Event] Premium Wind Serum
		{ 50, 162001031 }, // [Event] Premium Life Serum
		{ 50, 162001032 }, // [Event] Premium Mana Serum
		{ 50, 162001033 }, // [Event] Premium Recovery Serum
		{ 50, 164000256 }, // Fine Crit Spell Scroll
		{ 50, 164000255 }, // Fine Crit Strike Scroll
		{ 35, 164002117 }, // [Event] Major Enhanced Courage Scroll
		{ 35, 164002118 }, // [Event] Major Enhanced Awakening Scroll
		{ 25, 186000237 }, // Ancient coin
		{ 5, 166000195 } // Epsilon Enchantment Stone
	};

	public Easter() {
		super("easter", "Exchanges " + ChatUtil.item(186000175) + " for prizes.");

		setParamInfo("Type in .easter <id> to get your reward:",
			"[1] - (" + rewards[0][0] + " eggs) Random item",
			"[2] - (" + rewards[1][0] + " eggs) " + ChatUtil.item(rewards[1][1]),
			"[3] - (" + rewards[2][0] + " eggs) " + ChatUtil.item(rewards[2][1]),
			"[4] - (" + rewards[3][0] + " eggs) " + ChatUtil.item(rewards[3][1]),
			"[5] - (" + rewards[4][0] + " eggs) " + ChatUtil.item(rewards[4][1]),
			"[6] - (" + rewards[5][0] + " eggs) " + ChatUtil.item(rewards[5][1])
		);
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		try {
			int rewardIndex = Integer.parseInt(params[0]) - 1;
			if (rewardIndex < 0 || rewardIndex >= rewards.length)
				throw new InvalidParameterException(null); // sends the list

			int cost = rewards[rewardIndex][0];
			if (player.getInventory().getItemCountByItemId(neededItem) < cost || !player.getInventory().decreaseByItemId(neededItem, cost))
				throw new InvalidParameterException("You need " + cost + " " + ChatUtil.item(neededItem) + " for this.");

			int count = 1;
			int itemId = rewards[rewardIndex][1];
			if (itemId == 0) {
				int rndIndex = Rnd.get(randomItems.length);
				count = randomItems[rndIndex][0];
				itemId = randomItems[rndIndex][1];
			}

			long notAddedCount = ItemService.addItem(player, itemId, count, true, new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_CASH_ITEM));
			if (notAddedCount > 0)
				log.warn("[Easter Event] " + notAddedCount + "/" + count + " of " + itemId + " could not be added.");
		} catch (NumberFormatException | InvalidParameterException e) {
			sendInfo(player, e instanceof InvalidParameterException ? e.getMessage() : "Invalid prize.");
		}
	}
}
