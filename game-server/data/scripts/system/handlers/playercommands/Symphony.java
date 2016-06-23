package playercommands;

import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Pad
 */
public class Symphony extends PlayerCommand {

	private static final Logger log = LoggerFactory.getLogger(Symphony.class);
	private static final int requiredItem = 182007170;
	private static final int[][] rewards = { 
		// COLLECTION_COUNT, REWARD_ID, REWARD_COUNT
		{ 3, 188052719, 1 },		// [Event] Dye Bundle
		{ 5, 188052195, 1 },		// [Event] Legion Coin Chest
		{ 10, 186000238, 10 },	// Conqueror's Mark
		{ 15, 186000236, 10 },	// Blood Mark
		{ 50, 166500002, 1 },		// Amplification Stone
		{ 75, 188053113, 1 },		// Ahserion's Flight Ancient Manastone Bundle
		{ 100, 188053295, 1 },	// Empyrean Plume Chest
		{ 150, 166030005, 1 },	// Tempering Solution
		{ 200, 188053618, 1 },	// Honorable Elim's Idian Bundle
		{ 250, 190100143, 1 },	// Seakissed Aetherboard
		{ 300, 187000121, 1 },	// Hyperion Wings
	};

	public Symphony() {
		super("symphony", "Exchanges " + ChatUtil.item(requiredItem) + " for prizes.");

		setParamInfo("Type .symphony <id> to get your reward:",
			"[1] - (" + rewards[0][0] + " copies): " + rewards[0][2] + "*" + ChatUtil.item(rewards[0][1]),
			"[2] - (" + rewards[1][0] + " copies): " + rewards[1][2] + "*" + ChatUtil.item(rewards[1][1]),
			"[3] - (" + rewards[2][0] + " copies): " + rewards[2][2] + "*" + ChatUtil.item(rewards[2][1]),
			"[4] - (" + rewards[3][0] + " copies): " + rewards[3][2] + "*" + ChatUtil.item(rewards[3][1]),
			"[5] - (" + rewards[4][0] + " copies): " + rewards[4][2] + "*" + ChatUtil.item(rewards[4][1]),
			"[6] - (" + rewards[5][0] + " copies): " + rewards[5][2] + "*" + ChatUtil.item(rewards[5][1]),
			"[7] - (" + rewards[6][0] + " copies): " + rewards[6][2] + "*" + ChatUtil.item(rewards[6][1]),
			"[8] - (" + rewards[7][0] + " copies): " + rewards[7][2] + "*" + ChatUtil.item(rewards[7][1]),
			"[9] - (" + rewards[8][0] + " copies): " + rewards[8][2] + "*" + ChatUtil.item(rewards[8][1]),
			"[10] - (" + rewards[9][0] + " copies): " + rewards[9][2] + "*" + ChatUtil.item(rewards[9][1]),
			"[11] - (" + rewards[10][0] + " copies): " + rewards[10][2] + "*" + ChatUtil.item(rewards[10][1])
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
				throw new InvalidParameterException(null);

			int cost = rewards[rewardIndex][0];
			if (player.getInventory().getItemCountByItemId(requiredItem) < cost || !player.getInventory().decreaseByItemId(requiredItem, cost))
				throw new InvalidParameterException("You need " + cost + " " + ChatUtil.item(requiredItem) + " for this.");

			int itemId = rewards[rewardIndex][1];
			int itemCount = rewards[rewardIndex][2];
			
			long notAddedCount = ItemService.addItem(player, itemId, itemCount, true, new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_CASH_ITEM));
			if (notAddedCount > 0) {
				log.warn("[Legendary Symphony Event] " + notAddedCount + "*" + + itemId + " could not be added to " + player.getName() + "'s inventory.");
			}
		} catch (NumberFormatException | InvalidParameterException e) {
			sendInfo(player, e instanceof InvalidParameterException ? e.getMessage() : "Invalid prize.");
		}
	}
}