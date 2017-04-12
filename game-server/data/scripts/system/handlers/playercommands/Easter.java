package playercommands;

import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Neon
 */
public class Easter extends PlayerCommand {

	private static final Logger log = LoggerFactory.getLogger(Easter.class);
	private static int neededItem = 186000175;
	private static int[][] rewards = { { 25, 185000216 }, { 30, 186000101 }, { 30, 186000102 }, { 50, 186000236 }, { 150, 188053295 },
		{ 250, 188053702 }, { 500, 188053005 } };

	public Easter() {
		super("easter", "Exchanges " + ChatUtil.item(186000175) + " for prizes.");

		setSyntaxInfo("Type in .easter <id> to get your reward:", "[1] - (" + rewards[0][0] + " eggs) " + ChatUtil.item(rewards[0][1]),
			"[2] - (" + rewards[1][0] + " eggs) " + " 5x " + ChatUtil.item(rewards[1][1]) + " or 5x " + ChatUtil.item(rewards[1][1] + 3),
			"[3] - (" + rewards[2][0] + " eggs) " + " 5x " + ChatUtil.item(rewards[2][1]) + " or 5x " + ChatUtil.item(rewards[2][1] + 3),
			"[4] - (" + rewards[3][0] + " eggs) " + " 50x " + ChatUtil.item(rewards[3][1]),
			"[5] - (" + rewards[4][0] + " eggs) " + ChatUtil.item(rewards[4][1]), "[6] - (" + rewards[5][0] + " eggs) " + ChatUtil.item(rewards[5][1]),
			"[7] - (" + rewards[6][0] + " eggs) " + ChatUtil.item(rewards[6][1]));
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

			switch (itemId) {
				case 186000101: // Radiant Token
					count = 5;
					itemId = player.getRace() == Race.ASMODIANS ? itemId = 186000104 : itemId;
					break;
				case 186000102: // Circle Token
					count = 5;
					itemId = player.getRace() == Race.ASMODIANS ? itemId = 186000105 : itemId;
					break;
				case 186000236: // Blood Mark
					count = 50;
					break;
			}

			long notAddedCount = ItemService.addItem(player, itemId, count, true,
				new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_CASH_ITEM));
			if (notAddedCount > 0)
				log.warn("[Easter Event] " + notAddedCount + "/" + count + " of " + itemId + " could not be added.");
		} catch (NumberFormatException | InvalidParameterException e) {
			sendInfo(player, e instanceof InvalidParameterException ? e.getMessage() : "Invalid prize.");
		}
	}
}
