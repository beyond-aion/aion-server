package admincommands;

import org.apache.commons.lang3.math.NumberUtils;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.AdminService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Phantom, ATracer, Source
 */
public class Add extends AdminCommand {

	public Add() {
		super("add", "Adds items to the targets inventory.");

		// @formatter:off
		setSyntaxInfo(
			"<item link|ID> [count] - Adds the specified item(s) to your inventory.",
			"<player> <item link|ID> [count] - Adds the specified item(s) to the players inventory."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 1) {
			sendInfo(player);
			return;
		}

		int index = 0;
		Player receiver = player;
		int itemId = ChatUtil.getItemId(params[index]);
		if (itemId == 0) {
			String playerName = Util.convertName(params[index]);
			receiver = World.getInstance().getPlayer(playerName);
			if (receiver == null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
				return;
			}
			if (++index < params.length)
				itemId = ChatUtil.getItemId(params[index]);
		}

		ItemTemplate itemTemplate;
		if (itemId == 0 || (itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId)) == null) {
			sendInfo(player, "Invalid item.");
			return;
		}

		long itemCount = params.length > ++index ? NumberUtils.toLong(params[index]) : 1;
		if (itemCount <= 0 || itemCount / itemTemplate.getMaxStackCount() > 126) {
			sendInfo(player, "Invalid item count.");
			return;
		}

		if (!AdminService.getInstance().canOperate(player, receiver, itemId, "command //add"))
			return;

		long count = ItemService.addItem(receiver, itemId, itemCount, true);

		if (count == 0) {
			if (player != receiver) {
				PacketSendUtility.sendMessage(player, "You gave " + itemCount + " x [item:" + itemId + "] to " + receiver.getName() + ".");
				PacketSendUtility.sendMessage(receiver, "You received " + itemCount + " x [item:" + itemId + "] from " + player.getName() + ".");
			}
		} else {
			PacketSendUtility.sendMessage(player, "Item couldn't be added");
		}
	}

}
