package playercommands;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Neon, Estrayl, Farlon
 */
public class Easter extends PlayerCommand {

	private static final Logger log = LoggerFactory.getLogger(Easter.class);
	private static final int neededItem = 186000175;
	private static final List<Reward> rewards = List.of(
		new Reward(50, 186000147, 2), // Mithril Medal
		new Reward(50, 186000055, 3), // Major Ancient Goblet
		new Reward(75, 166020000, 5), // Omega Enchantment Stone
		new Reward(75, 188053609, 3), // [Event] Level 60 Composite Manastone Bundle
		new Reward(75, 166200013, 1), // Enduring Mythic Weapon Tuning Scroll
		new Reward(75, 188053113, 3), // Ahserion's Flight Ancient Manastone Bundle
		new Reward(100, 188053295, 1), // Empyrean Plume Chest
		new Reward(100, 166030005, 5), // Tempering Solution
		new Reward(300, 188053702, 1) // Vasharti's Equipment Box
	);
	private static final List<Reward> randomRewards = List.of(
		new Reward(25, 162002030, 10), // [Event] Premium Restoration Serum
		new Reward(25, 186000237, 50), // Ancient Coin
		new Reward(25, 162000137, 3), // Sublime Life Serum
		new Reward(25, 162000139, 3), // Sublime Mana Serum
		new Reward(25, 186000146, 5), // Guestpetal
		new Reward(25, 188054198, 1), // Greater Scroll Bundle
		new Reward(25, 164000126, 10), // Major Strike Resist Scroll
		new Reward(25, 164000130, 10) // Major Spell Resist Scroll
	);

	public Easter() {
		super("easter", "Exchanges " + ChatUtil.item(186000175) + " for prizes.", buildSyntaxInfo());
	}

	private static String buildSyntaxInfo() {
		String syntaxInfo = "Type in .easter <ID> to get your reward:";
		int i = 1;
		syntaxInfo += "\n[" + i++ + "] - (" + randomRewards.getFirst().requiredEggs + " eggs) Random item";
		for (Reward r : rewards)
			syntaxInfo += "\n[" + i++ + "] - (" + r.requiredEggs + " eggs) " + r.itemCount + "x " + ChatUtil.item(r.itemId);
		return syntaxInfo;
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}
		int rewardIndex = Integer.parseInt(params[0]) - 1;
		if (rewardIndex < 0 || rewardIndex >= rewards.size() + 1) {
			sendInfo(player, "Invalid reward ID.");
			return;
		}
		Reward reward = rewardIndex == 0 ? Rnd.get(randomRewards) : rewards.get(rewardIndex - 1);
		int cost = reward.requiredEggs;
		if (player.getInventory().getItemCountByItemId(neededItem) < cost || !player.getInventory().decreaseByItemId(neededItem, cost)) {
			sendInfo(player, "You need " + cost + " " + ChatUtil.item(neededItem) + " for this.");
			return;
		}
		long notAddedCount = ItemService.addItem(player, reward.itemId, reward.itemCount, true,
			new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_CASH_ITEM));
		if (notAddedCount > 0)
			log.warn("[Easter Event] " + notAddedCount + "/" + reward.itemCount + " of " + reward.itemId + " could not be added.");
	}

	private record Reward(int requiredEggs, int itemId, long itemCount) {}
}
