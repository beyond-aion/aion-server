package playercommands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.AbstractItemAction;
import com.aionemu.gameserver.model.templates.item.actions.EmotionLearnAction;
import com.aionemu.gameserver.model.templates.item.enums.EquipType;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.itemset.ItemPart;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.*;
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
		super("preview", "Previews equipment and emotion cards.");

		// @formatter:off
		setSyntaxInfo(
			"<emotion card item> - Previews the emotion.",
			"<color> - Previews your equipped items in the specified color (dye item, color name or color HEX code).",
			"<item(s)> [color] - Previews the specified equipment on your character (default: standard item color, optional: dye item, color name or color HEX code).",
			"Multiple items can be separated by commas or spaces.",
			"If a single item is given and it's a part of an item set, you will get a preview of the whole item set."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}

		List<ItemParam> itemParams = parse(params);
		if (!previewEmotion(player, itemParams.getFirst()))
			previewEquipment(player, itemParams);
	}

	private boolean previewEmotion(Player player, ItemParam itemParam) {
		if (itemParam.itemTemplate == null || itemParam.itemTemplate.getActions() == null)
			return false;
		for (AbstractItemAction itemAction : itemParam.itemTemplate.getActions().getItemActions()) {
			if (itemAction instanceof EmotionLearnAction emotionLearnAction) {
				if ((player.getState() & ~CreatureState.POWERSHARD.getId()) > CreatureState.ACTIVE.getId()) { // prevent bugged animations
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_CAST_IN_CURRENT_STANCE());
				} else {
					int targetObjectId = player.getTarget() != null ? player.getTarget().getObjectId() : 0;
					PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.EMOTE_END));
					PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.EMOTE, emotionLearnAction.getEmotionId(), targetObjectId));
				}
				return true;
			}
		}
		return false;
	}

	private void previewEquipment(Player player, List<ItemParam> itemParams) {
		Integer itemColor = null; // null = default item color
		String colorText = "default";
		for (ItemParam itemParam : itemParams) {
			itemColor = itemParam.dyeColor();
			if (itemColor != null) {
				if (itemParam.itemTemplate == null)
					colorText = ChatUtil.color("#" + String.format("%06X", itemColor & 0xFFFFFF), itemColor);
				else
					colorText = ChatUtil.item(itemParam.itemTemplate.getTemplateId());
				itemParams.remove(itemParam);
				break;
			}
		}
		previewEquipment(player, itemParams.stream().map(ItemParam::itemTemplate).toList(), itemColor, colorText);
	}

	private void previewEquipment(Player player, List<ItemTemplate> equipment, Integer itemColor, String colorText) {
		if (!equipment.stream().allMatch(itemTemplate -> validateForPreview(player, itemTemplate)))
			return;
		if (itemColor != null && !equipment.isEmpty() && equipment.stream().noneMatch(ItemTemplate::isItemDyePermitted)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_CHANGE_ERROR_CANNOTDYE(equipment.getFirst().getL10n()));
			return;
		}
		String itemNames = "";
		long previewItemsSlotMask = 0;
		List<Item> previewItems = new ArrayList<>();
		if (equipment.size() == 1 && equipment.getFirst().isItemSet()) { // preview whole set
			ItemSetTemplate itemSet = equipment.getFirst().getItemSet();
			for (ItemPart part : itemSet.getItempart())
				previewItemsSlotMask |= addFakeItem(previewItems, previewItemsSlotMask, part.getItemId(), itemColor);
		} else {
			for (ItemTemplate template : equipment)
				previewItemsSlotMask |= addFakeItem(previewItems, previewItemsSlotMask, template.getTemplateId(), itemColor);
		}
		int previewRobotId = 0;
		for (Item previewItem : previewItems) {
			itemNames += "\n\t" + ChatUtil.item(previewItem.getItemId());
			if (player.isInRobotMode() && previewItem.getItemTemplate().getItemGroup() == ItemGroup.KEYBLADE)
				previewRobotId = previewItem.getItemTemplate().getRobotId();
		}

		addOwnEquipment(player, previewItems, previewItemsSlotMask, itemColor);
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
		if (equipment.isEmpty())
			sendInfo(player, "Previewing your equipment for " + PREVIEW_TIME_SECONDS + " seconds in color " + colorText);
		else
			sendInfo(player, "Previewing the following items for " + PREVIEW_TIME_SECONDS + " seconds (color: " + colorText + "):" + itemNames);
	}

	private boolean validateForPreview(Player player, ItemTemplate itemTemplate) {
		if (itemTemplate == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CHANGE_ITEM_SKIN_NO_TARGET_ITEM());
			return false;
		} else if (itemTemplate.getEquipmentType() == EquipType.NONE) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_CHANGE_ITEM_SKIN_PREVIEW_INVALID_COSMETIC());
			return false;
		} else if (itemTemplate.getRace() == player.getOppositeRace()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PREVIEW_INVALID_RACE());
			return false;
		} else if (itemTemplate.getUseLimits().getGenderPermitted() != null && itemTemplate.getUseLimits().getGenderPermitted() != player.getGender()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PREVIEW_INVALID_GENDER());
			return false;
		} else if (!ItemSlot.isVisible(itemTemplate.getItemSlot())) {
			sendInfo(player, itemTemplate.getL10n() + " is no visible equipment.");
			return false;
		}
		return true;
	}

	private List<ItemParam> parse(String[] params) {
		List<ItemParam> itemParams = new ArrayList<>();
		for (String param : params) {
			String[] ids = param.split(",|(?<=[^,])(?=\\[)|(?<=[\\]])(?=[^\\[])"); // split on comma and between item tags (square brackets)
			for (String id : ids) {
				itemParams.add(new ItemParam(id, DataManager.ITEM_DATA.getItemTemplate(ChatUtil.getItemId(id))));
			}
		}
		return itemParams;
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
		if (itemTemplate.isTwoHandWeapon() && occupiedSlots != 0) // only allow two-handed if both hands are free
			return 0;
		if (!itemTemplate.isTwoHandWeapon())
			itemSlotMask = getFirstFreeSlot(itemSlotMask, occupiedSlots); // select the correct slot for CL_MULTISLOT, weapons, earrings and power shards
		Item previewItem = new Item(0, itemTemplate, 1, true, itemSlotMask); // ObjId 0 to avoid allocating new IDFactory IDs (it'll not be used anywhere)
		if (itemTemplate.isItemDyePermitted())
			previewItem.setItemColor(itemColor);
		items.add(previewItem);
		return itemSlotMask;
	}

	private static long getFirstFreeSlot(long targetSlots, long occupiedSlots) {
		long freeSlots = targetSlots & ~occupiedSlots;
		return Long.lowestOneBit(freeSlots);
	}

	private static void addOwnEquipment(Player player, List<Item> previewItems, long previewItemsSlotMask, Integer itemColor) {
		boolean previewContainsMainHandWeapon = (previewItemsSlotMask & ItemSlot.MAIN_HAND.getSlotIdMask()) != 0;
		for (Item visibleEquipment : player.getEquipment().getEquippedForAppearance()) {
			if (previewContainsMainHandWeapon && visibleEquipment.getItemTemplate().isOneHandWeapon())
				continue; // don't show own weapon in off-hand if player wants to preview a specific weapon
			previewItemsSlotMask |= addFakeItem(previewItems, previewItemsSlotMask, visibleEquipment.getItemId(), itemColor);
		}
	}

	private static void schedulePreviewReset(Player player, int duration, boolean previewRobot) {
		PREVIEW_RESETS.compute(player.getObjectId(), (_, resetTask) -> {
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
			}, duration * 1000L);
			return resetTask;
		});
	}

	private static void updateRobotAppearance(Player player, int robotId) {
		PacketSendUtility.sendPacket(player, new SM_RIDE_ROBOT(player, 0));
		PacketSendUtility.sendPacket(player, new SM_RIDE_ROBOT(player, robotId));
	}

	private record ItemParam(String input, ItemTemplate itemTemplate) {

		private Integer dyeColor() {
			if (itemTemplate != null) {
				if (itemTemplate.getActions() == null || itemTemplate.getActions().getDyeAction() == null)
					return null;
				return itemTemplate.getActions().getDyeAction().getColor();
			}
			String colorParam = input;
			try {
				// try to get color by name
				return ((Color) Color.class.getField(colorParam.toUpperCase()).get(null)).getRGB();
			} catch (Exception e) {
				// try to get color by hex code
				if (colorParam.length() <= 8) {
					if (colorParam.startsWith("#"))
						colorParam = colorParam.substring(1);
					else if (colorParam.startsWith("0x") || colorParam.startsWith("0X"))
						colorParam = colorParam.substring(2);
				}
				try {
					return Integer.valueOf(colorParam, 16);
				} catch (NumberFormatException _) {
					return null;
				}
			}
		}
	}
}
