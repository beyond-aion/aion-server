package playercommands;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;
import javolution.util.FastTable;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.EquipType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_PLAYER_APPEARANCE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Neon
 */
public class Preview extends PlayerCommand {

	private static final Map<Integer, ScheduledFuture<?>> PREVIEW_RESETS = new FastMap<Integer, ScheduledFuture<?>>().shared();
	private static final byte MAX_PLAYER_PREVIEWS = 40;
	private static final byte PREVIEW_TIME = 10;

	public Preview() {
		super("preview", "Previews equipment.");

		setParamInfo("<item> [color] - Previews the specified item on your character (default: standard item color, optional: dye item link/ID, color name or color HEX code).");
	}

	@Override
	public void execute(Player player, String... params) {
		if (player.getLevel() < 10) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_PC_LEVEL_LIMIT());
			return;
		}

		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		if (PREVIEW_RESETS.size() >= MAX_PLAYER_PREVIEWS) {
			sendInfo(player, "There are currently too many players using this command. Please wait a moment.");
			return;
		}

		int itemId = ChatUtil.getItemId(params[0]);
		if (itemId == 0) {
			sendInfo(player, "Invalid item.");
			return;
		}

		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
		if (itemTemplate == null) {
			sendInfo(player, "Unknown item.");
			return;
		} else if (itemTemplate.getEquipmentType().equals(EquipType.NONE)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CHANGE_ITEM_SKIN_PREVIEW_INVALID_COSMETIC());
			return;
		} else if (!itemTemplate.getRace().equals(Race.PC_ALL) && !itemTemplate.getRace().equals(player.getRace())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PREVIEW_INVALID_RACE());
			return;
		} else {
			Gender itemGender = itemTemplate.getUseLimits().getGenderPermitted();
			if (itemGender != null && !itemGender.equals(player.getGender())) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PREVIEW_INVALID_GENDER());
				return;
			}
		}

		if (!ItemSlot.isVisible(itemTemplate.getItemSlot())) {
			sendInfo(player, "Item is no visible equipment.");
			return;
		}

		int itemColor = 0; // 0 = default item color
		String colorText = "default";
		if (params.length > 1) {
			if (!itemTemplate.isItemDyePermitted()) {
				sendInfo(player, "The specified item can not be dyed.");
				return;
			}

			// try to get itemId of a dyeing item
			String colorParam = params[1];
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

		// create fake item for preview (ObjId 0, since it'll not be used anywhere and to avoid allocating new IDFactory IDs)
		Item previewItem = new Item(0, itemTemplate, 1, true, itemTemplate.getItemSlot());
		previewItem.setItemColor(itemColor);

		registerPreviewReset(player, PREVIEW_TIME);
		PacketSendUtility.sendPacket(player, new SM_UPDATE_PLAYER_APPEARANCE(player.getObjectId(), getPreviewItems(player, previewItem)));
		sendInfo(player, "Previewing " + ChatUtil.item(itemId) + " for " + PREVIEW_TIME + " seconds (color: " + colorText + ").");
	}

	private static List<Item> getPreviewItems(Player player, Item previewItem) {
		long previewSlot = previewItem.getEquipmentSlot();
		List<Item> previewItems = FastTable.of(player.getEquipment().getEquippedForAppearence());

		if (ItemSlot.isTwoHandedWeapon(previewSlot))
			// remove shield or sub hand weapons if previewing TwoHanded
			previewItems.removeIf(item -> item.getEquipmentSlot() == ItemSlot.SUB_HAND.getSlotIdMask());
		
		// put item in the new appearance list (at the correct position)
		for (int i = 0; i < previewItems.size(); i++) {
			long currentSlot = previewItems.get(i).getEquipmentSlot();
			if ((currentSlot & previewSlot) != 0) {
				// replace players current appearance item at that slot
				previewItems.set(i, previewItem);
				break;
			} else if (currentSlot > previewSlot) {
				// player currently has no item in the corresponding slot, so insert preview item here
				previewItems.add(i, previewItem);
				break;
			} else if (i + 1 == previewItems.size()) {
				// last entry, add behind
				previewItems.add(previewItem);
			}
		}

		return previewItems;
	}

	private static void registerPreviewReset(final Player player, int duration) {
		int objId = player.getObjectId();

		// cancel previous scheduled preview reset thread
		if (PREVIEW_RESETS.containsKey(objId))
			PREVIEW_RESETS.get(objId).cancel(true);

		ScheduledFuture<?> resetThread = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (player.isOnline()) {
					PacketSendUtility.sendPacket(player, new SM_UPDATE_PLAYER_APPEARANCE(objId, player.getEquipment().getEquippedForAppearence()));
					PacketSendUtility.sendMessage(player, "Preview time ended.");
				}
				PREVIEW_RESETS.remove(objId);
			}
		}, duration * 1000);

		PREVIEW_RESETS.put(objId, resetThread);
	}
}
