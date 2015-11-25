package admincommands;

import java.awt.Color;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_PLAYER_APPEARANCE;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author loleron
 * @reworked Neon
 */
public class Dye extends AdminCommand {

	public Dye() {
		super("dye", "Dyes a players visible equipment.");

		setParamInfo("<color> - Dyes the selected player in the specified color (can be dye item link/ID, color name or color HEX code). 0 removes all dyeing.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		Player target = player;
		if (player.getTarget() instanceof Player)
			target = (Player) player.getTarget();

		int itemColor = 0; // 0 = default item color
		String colorText = "default";
		String colorParam = params[0];

		if (!"0".equalsIgnoreCase(colorParam)) {
			// try to get itemId of a dyeing item
			itemColor = ChatUtil.getItemId(colorParam);
			ItemTemplate dyeItemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemColor);

			if (itemColor != 0 && dyeItemTemplate != null && dyeItemTemplate.getActions() != null && dyeItemTemplate.getActions().getDyeAction() != null) {
				// itemColor is a dyeing item ID
				colorText = ChatUtil.item(itemColor);
			} else {
				try {
					try {
						// try to get color by name
						itemColor = ((Color) Class.forName("java.awt.Color").getField(colorParam.toUpperCase()).get(null)).getRGB();
					} catch (Exception e) {
						// try to get color by hex code
						if (colorParam.length() <= 8) {
							if (colorParam.startsWith("#"))
								colorParam = colorParam.substring(1);
							else if (colorParam.startsWith("0x") || colorParam.startsWith("0X"))
								colorParam = colorParam.substring(2);
						}
						itemColor = Integer.valueOf(colorParam, 16);
					}
					colorText = ChatUtil.color("#" + String.format("%06X", itemColor & 0xFFFFFF), itemColor);
					itemColor = Util.toColorBGRA(itemColor);
				} catch (NumberFormatException e) {
					sendInfo(player, "Invalid color.");
					return;
				}
			}
		}

		List<Item> appearanceItems = target.getEquipment().getEquippedForAppearence();
		for (Item item : appearanceItems) {
			if (item.getItemSkinTemplate().isItemDyePermitted())
				item.setItemColor(itemColor);
			ItemPacketService.updateItemAfterInfoChange(target, item);
		}
		PacketSendUtility.broadcastPacket(target, new SM_UPDATE_PLAYER_APPEARANCE(target.getObjectId(), appearanceItems), true);
		target.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);

		if (itemColor == 0)
			sendInfo(player, "Removed dyeing from " + target.getName() + "'s visible equipment.");
		else
			sendInfo(player, "Dyed " + target.getName() + " (color: " + colorText + ")");

		if (!target.getObjectId().equals(player.getObjectId()))
			sendInfo(target, player.getName() + " has changed the color of your visible equipment to: " + colorText);
	}
}
