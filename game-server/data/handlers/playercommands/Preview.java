package playercommands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.EquipType;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.itemset.ItemPart;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RIDE_ROBOT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_PLAYER_APPEARANCE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Neon
 */
public class Preview extends PlayerCommand {

	private static final Map<Integer, ScheduledFuture<?>> PREVIEW_RESETS = new ConcurrentHashMap<>();
	private static final int PREVIEW_TIME_SECONDS = 10;

	public Preview() {
		super("preview", "Previews equipment.");

		// @formatter:off
		setSyntaxInfo(
			"<color> - Previews your equipped items in the specified color (dye item, color name or color HEX code).",
			"<item(s)> [color] - Previews the specified item(s) on your character (default: standard item color, optional: dye item, color name or color HEX code).",
			"Multiple items may be separated with commas, but not with spaces.",
			"If a single item is given and it's a part of an item set, you will get a preview of the whole item set."
		);
		// @formatter:on
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

		int i = 0;
		boolean onlyColor = params.length == 1 && !params[0].contains("[") && !params[0].matches("([1-9][0-9]{8},?)+");
		List<ItemTemplate> items = new ArrayList<>();
		if (onlyColor)
			player.getEquipment().getEquippedForAppearance().forEach(item -> items.add(item.getItemTemplate()));
		else if (!parseItems(player, params[i++], items))
			return;

		Integer itemColor = null; // null = default item color
		String colorText = "default";
		if (params.length > i) {
			// try to get itemId of a dyeing item
			String colorParam = params[i];
			itemColor = ChatUtil.getItemId(colorParam);
			ItemTemplate dyeItemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemColor);

			if (itemColor != 0 && dyeItemTemplate != null && dyeItemTemplate.getActions() != null && dyeItemTemplate.getActions().getDyeAction() != null) {
				// itemColor is a dyeing item ID
				itemColor = dyeItemTemplate.getActions().getDyeAction().getColor();
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
				} catch (NumberFormatException e) {
					sendInfo(player, colorParam + " is not a valid color.");
					return;
				}
			}
			if (items.stream().noneMatch(ItemTemplate::isItemDyePermitted)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_CHANGE_ERROR_CANNOTDYE(items.get(0).getL10n()));
				return;
			}
		}

		String itemNames = "";
		long previewItemsSlotMask = 0;
		List<Item> previewItems = new ArrayList<>();
		if (items.size() == 1 && items.get(0).isItemSet()) { // preview whole set
			ItemSetTemplate itemSet = items.get(0).getItemSet();
			for (ItemPart part : itemSet.getItempart())
				previewItemsSlotMask |= addFakeItem(previewItems, previewItemsSlotMask, part.getItemId(), itemColor);
		} else {
			for (ItemTemplate template : items)
				previewItemsSlotMask |= addFakeItem(previewItems, previewItemsSlotMask, template.getTemplateId(), itemColor);
		}
		int previewRobotId = 0;
		for (Item previewItem : previewItems) {
			itemNames += "\n\t" + ChatUtil.item(previewItem.getItemId());
			if (player.isInRobotMode() && previewItem.getItemTemplate().getItemGroup() == ItemGroup.KEYBLADE)
				previewRobotId = previewItem.getItemTemplate().getRobotId();
		}

		addOwnEquipment(player, previewItems, previewItemsSlotMask);
		previewItems.sort(Comparator.comparingLong(Item::getEquipmentSlot)); // order by equipment slot ids (ascending) to avoid display bugs
		int display = player.getPlayerSettings().getDisplay() | SM_CUSTOM_SETTINGS.HIDE_LEGION_CLOAK;
		if (previewItems.stream().anyMatch(item -> item.getEquipmentSlot() == ItemSlot.HELMET.getSlotIdMask())) {
			display &= ~SM_CUSTOM_SETTINGS.HIDE_HELMET;
		}
		if (previewItems.stream().anyMatch(item -> item.getEquipmentSlot() == ItemSlot.PLUME.getSlotIdMask())) {
			display &= ~SM_CUSTOM_SETTINGS.HIDE_PLUME;
		}
		PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(player.getObjectId(), 1, display, player.getPlayerSettings().getDeny()));
		PacketSendUtility.sendPacket(player, new SM_UPDATE_PLAYER_APPEARANCE(player.getObjectId(), previewItems));
		int switchRobotAnimationSeconds = 0;
		if (previewRobotId != 0) {
			switchRobotAnimationSeconds = 1;
			updateRobotAppearance(player, previewRobotId);
		}
		schedulePreviewReset(player, PREVIEW_TIME_SECONDS + switchRobotAnimationSeconds, previewRobotId != 0);
		if (onlyColor)
			sendInfo(player, "Previewing your equipment for " + PREVIEW_TIME_SECONDS + " seconds in color " + colorText);
		else
			sendInfo(player, "Previewing the following items for " + PREVIEW_TIME_SECONDS + " seconds (color: " + colorText + "):" + itemNames);
	}

	private boolean parseItems(Player player, String param, List<ItemTemplate> items) {
		String[] ids = param.split(",|(?<=[^,])(?=\\[)|(?<=[\\]])(?=[^\\[])"); // split on , and between item tags (square brackets)
		for (String id : ids) {
			int itemId = ChatUtil.getItemId(id);
			if (itemId == 0) {
				sendInfo(player, "Invalid item specified.");
				return false;
			}

			ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
			if (itemTemplate == null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_NO_TARGET_ITEM());
				return false;
			} else if (itemTemplate.getEquipmentType() == EquipType.NONE) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CHANGE_ITEM_SKIN_PREVIEW_INVALID_COSMETIC());
				return false;
			} else if (itemTemplate.getRace() == player.getOppositeRace()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PREVIEW_INVALID_RACE());
				return false;
			} else {
				Gender itemGender = itemTemplate.getUseLimits().getGenderPermitted();
				if (itemGender != null && itemGender != player.getGender()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PREVIEW_INVALID_GENDER());
					return false;
				}
			}

			if (!ItemSlot.isVisible(itemTemplate.getItemSlot())) {
				sendInfo(player, "Item is no visible equipment.");
				return false;
			}

			items.add(itemTemplate);
		}
		return true;
	}

	/**
	 * @return Equipment slot mask of the preview item, 0 if it was not added
	 */
	private static long addFakeItem(List<Item> items, long previewItemsSlotMask, int itemId, Integer itemColor) {
		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
		long itemSlotMask = itemTemplate.getItemSlot();
		if (!ItemSlot.isVisible(itemSlotMask)) // don't add invisible items (like rings or belts)
			return 0;
		long occupiedSlots = previewItemsSlotMask & itemSlotMask;
		if (occupiedSlots == itemSlotMask) // an item of that kind is already present in the list
			return 0;
		if (itemSlotMask == ItemSlot.EARRING_RIGHT_OR_LEFT.getSlotIdMask()) {
			if (occupiedSlots == 0)
				itemSlotMask = ItemSlot.EARRINGS_LEFT.getSlotIdMask();
			else if (occupiedSlots == ItemSlot.EARRINGS_LEFT.getSlotIdMask())
				itemSlotMask = ItemSlot.EARRINGS_RIGHT.getSlotIdMask();
		}
		Item previewItem = new Item(0, itemTemplate, 1, true, itemSlotMask); // ObjId 0 to avoid allocating new IDFactory IDs (it'll not be used anywhere)
		if (itemTemplate.isItemDyePermitted())
			previewItem.setItemColor(itemColor);
		items.add(previewItem);
		return itemSlotMask;
	}

	private static void addOwnEquipment(Player player, List<Item> previewItems, long previewItemsSlotMask) {
		boolean previewTwoHanded = false;
		for (Item visibleEquipment : player.getEquipment().getEquippedForAppearance()) {
			if (previewTwoHanded || ItemSlot.isTwoHandedWeapon(visibleEquipment.getEquipmentSlot())) {
				previewTwoHanded = true;
				if (visibleEquipment.getEquipmentSlot() == ItemSlot.SUB_HAND.getSlotIdMask())
					continue; // don't add players shield or sub hand weapon if previewing TwoHanded
			}

			if ((visibleEquipment.getEquipmentSlot() & previewItemsSlotMask) == 0) // add rest of players equipment
				previewItems.add(visibleEquipment);
		}
	}

	private static void schedulePreviewReset(Player player, int duration, boolean previewRobot) {
		PREVIEW_RESETS.compute(player.getObjectId(), (k, resetTask) -> {
			if (resetTask != null) { // cancel previous scheduled preview reset thread
				if (!previewRobot && player.isInRobotMode()) // restore robot appearance in case it was previewed just a few seconds ago
					updateRobotAppearance(player, player.getRobotId());
				resetTask.cancel(true);
			}
			resetTask = ThreadPoolManager.getInstance().schedule(() -> {
				PacketSendUtility.sendPacket(player, new SM_CUSTOM_SETTINGS(player));
				PacketSendUtility.sendPacket(player, new SM_UPDATE_PLAYER_APPEARANCE(player.getObjectId(), player.getEquipment().getEquippedForAppearance()));
				if (previewRobot && player.isInRobotMode())
					updateRobotAppearance(player, player.getRobotId());
				PacketSendUtility.sendMessage(player, "Preview time ended.");
				PREVIEW_RESETS.remove(player.getObjectId());
			}, duration * 1000);
			return resetTask;
		});
	}

	private static void updateRobotAppearance(Player player, int robotId) {
		PacketSendUtility.sendPacket(player, new SM_RIDE_ROBOT(player, 0));
		PacketSendUtility.sendPacket(player, new SM_RIDE_ROBOT(player, robotId));
	}
}
