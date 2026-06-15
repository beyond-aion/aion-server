package playercommands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Pad
 */
public class Symphony extends PlayerCommand {

	private static final Logger log = LoggerFactory.getLogger(Symphony.class);
	private static final int REQUIRED_ITEM_ID = 182007170;
	private static final int[][] REWARDS = {
		// COLLECTION_COUNT, REWARD_ID, REWARD_COUNT
		{ 3, 186000236, 10 }, // Blood Mark
		{ 5, 186000399, 10 }, // Honorable Conqueror's Mark
		{ 15, 166000195, 5 }, // Epsilon Enchantment Stone
		{ 40, 188052388, 1 }, // Modor's Equipment Box
		{ 50, 188053695, 2 }, // High Grade Crafting Material Box of Conquest
		{ 50, 188053610, 3 }, // [Event] Level 70 Composite Manastone Bundle
		{ 60, 188053321, 1 }, // [Event] Empyrean Plume Chest
		{ 65, 188053903, 1 }, // Honorable Equipment of Conquest Box
		{ 70, 166020003, 10 }, // [Event] Omega Enchantment Stone
		{ 70, 166500005, 10 }, // [Event] Amplification Stone
		{ 70, 166030007, 10 }, // [Event] Tempering Solution
		{ 100, 188950015, 2 }, // Special Courier Pass (Eternal/Lv. 61-65)
		{ 150, 188053099, 1 }, // Pure Modor's Equipment Crux Box
		{ 200, 188054238, 1 }, // Iron Wall Armor Box
		{ 250, 187000090, 1 }, // Tiamat's Spectral Wings

	};

	public Symphony() {
		super("symphony", "Exchanges " + ChatUtil.item(REQUIRED_ITEM_ID) + " for prizes.");

		setSyntaxInfo(buildSyntaxInfo());
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		try {
			int rewardIndex = Integer.parseInt(params[0]) - 1;
			if (rewardIndex < 0 || rewardIndex >= REWARDS.length)
				throw new IllegalArgumentException();

			int cost = REWARDS[rewardIndex][0];
			if (player.getInventory().getItemCountByItemId(REQUIRED_ITEM_ID) < cost || !player.getInventory().decreaseByItemId(REQUIRED_ITEM_ID, cost))
				throw new IllegalArgumentException("You need %d %s to buy this.".formatted(cost, ChatUtil.item(REQUIRED_ITEM_ID)));

			int itemId = REWARDS[rewardIndex][1];
			int itemCount = REWARDS[rewardIndex][2];

			long notAddedCount = ItemService.addItem(player, itemId, itemCount, true,
				new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_CASH_ITEM));
			if (notAddedCount > 0) {
				log.warn("[Legendary Symphony Event] {}x {} could not be added to {}'s inventory.", notAddedCount, itemId, player.getName());
			}
		} catch (IllegalArgumentException e) {
			sendInfo(player, e instanceof NumberFormatException ? "Invalid prize." : e.getMessage());
		}
	}

	private String buildSyntaxInfo() {
		StringBuilder sb = new StringBuilder("Type .symphony <id> to get your reward:\n");

		for (int i = 0; i < REWARDS.length; i++) {
			int[] reward = REWARDS[i];
			sb.append("[").append(i + 1).append("] - (").append(reward[0]).append(" copies): ").append(reward[2]).append("x ")
				.append(ChatUtil.item(reward[1])).append("\n");
		}

		return sb.toString();
	}
}
