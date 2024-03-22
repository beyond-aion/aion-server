package com.aionemu.gameserver.services.event;

import java.time.Month;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUSTOM_SETTINGS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RIDE_ROBOT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPDATE_PLAYER_APPEARANCE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.time.ServerTime;

public class AprilFoolsAppearance {

	private static final Map<ItemGroup, List<Item>> overrides = createAppearanceItems();
	private static final Map<Integer, Appearance> appearanceByPlayerId = new ConcurrentHashMap<>();

	private static Map<ItemGroup, List<Item>> createAppearanceItems() {
		Map<ItemGroup, List<Item>> items = new HashMap<>();
		items.put(ItemGroup.SWORD, createItems(100000007)); // Kobold Dipper
		items.put(ItemGroup.GREATSWORD, createItems(100900279)); // Farmer's Rake01
		items.put(ItemGroup.DAGGER, createItems(100200734)); // Shulack Fan01 (NPC)
		items.put(ItemGroup.MACE, createItems(100001573)); // NPC Hammer
		items.put(ItemGroup.ORB, createItems(100501303)); // [Event] Orb of Rapture
		items.put(ItemGroup.SPELLBOOK, createItems(100600894)); // Maid Fruit Basket
		items.put(ItemGroup.POLEARM, createItems(101301247)); // Polearm of the Brave Heart
		items.put(ItemGroup.STAFF, createItems(101500288)); // NPC Broom01
		items.put(ItemGroup.BOW, createItems(101701358)); // [Event] Bow of Rapture
		items.put(ItemGroup.HARP, createItems(102001228)); // Stringed Instrument of the Brave Heart
		items.put(ItemGroup.GUN, createItems(101801071)); // NPC user-designed event pistol
		items.put(ItemGroup.CANNON, createItems(101901109)); // Aether Cannon of the Brave Heart
		items.put(ItemGroup.KEYBLADE, createItems(102101059)); // Aether Key of Rapture
		items.put(ItemGroup.SHIELD, createItems(115001743)); // Shield of the Brave Heart
		items.put(ItemGroup.HEAD, createItems(125000652, 125001925, 125002481, 125002482, 125003215, 125040024, 125040036, 125045088));
		return items;
	}

	private static List<Item> createItems(int... ids) {
		List<Item> items = new ArrayList<>();
		for (int id : ids) {
			ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(id);
			ItemSlot[] validSlots = ItemSlot.getSlotsFor(itemTemplate.getItemGroup().getValidEquipmentSlots());
			if (validSlots.length == 0) {
				throw new IllegalArgumentException(String.valueOf(id));
			}
			for (ItemSlot validSlot : validSlots) {
				Item item = new Item(0, itemTemplate);
				item.setEquipmentSlot(validSlot.getSlotIdMask());
				items.add(item);
			}
		}
		return items;
	}

	private static boolean isAprilFoolsDay() {
		ZonedDateTime now = ServerTime.now();
		return now.getDayOfMonth() == 1 && now.getMonth() == Month.APRIL;
	}

	public static void setAppearance(Player player) {
		Appearance appearance = createAppearance(player);
		if (appearance == null)
			return;
		appearanceByPlayerId.put(player.getObjectId(), appearance);
		if (appearance.robotId != 0 && player.isInRobotMode()) {
			PacketSendUtility.broadcastPacket(player, new SM_RIDE_ROBOT(player, 0), true);
			PacketSendUtility.broadcastPacket(player, new SM_RIDE_ROBOT(player), true);
		}
		PacketSendUtility.sendPacket(player, new SM_UPDATE_PLAYER_APPEARANCE(player.getObjectId(), appearance.items));
	}

	private static Appearance createAppearance(Player player) {
		if (!isAprilFoolsDay())
			return null;
		int robotId = 0;
		List<Item> equippedForAppearance = new ArrayList<>();
		List<Item> weapons = Arrays.asList(player.getEquipment().getMainHandWeapon(), player.getEquipment().getOffHandWeapon());
		for (Item weapon : weapons) {
			if (weapon == null)
				continue;
			Item override = overrides.getOrDefault(weapon.getItemTemplate().getItemGroup(), Collections.emptyList()).stream()
															 .filter(i -> (weapon.getEquipmentSlot() & i.getEquipmentSlot()) == i.getEquipmentSlot())
															 .findFirst()
															 .orElse(weapon);
			equippedForAppearance.add(override);
			if (override.getItemTemplate().getItemGroup() == ItemGroup.KEYBLADE)
				robotId = override.getItemTemplate().getRobotId();
		}
		List<Item> helmets = overrides.get(ItemGroup.HEAD);
		if (helmets != null) {
			equippedForAppearance.add(helmets.get(player.getObjectId() % helmets.size()));
			int display = player.getPlayerSettings().getDisplay() & ~SM_CUSTOM_SETTINGS.HIDE_HELMET;
			PacketSendUtility.broadcastPacket(player, new SM_CUSTOM_SETTINGS(player.getObjectId(), 1, display, player.getPlayerSettings().getDeny()), true);
		}
		return new Appearance(equippedForAppearance, robotId);
	}

	public static void resetAppearance(Player player) {
		final Appearance appearance = appearanceByPlayerId.remove(player.getObjectId());
		if (appearance == null)
			return;
		PacketSendUtility.broadcastPacket(player, new SM_CUSTOM_SETTINGS(player), true);
		PacketSendUtility.broadcastPacket(player, new SM_UPDATE_PLAYER_APPEARANCE(player.getObjectId(), player.getEquipment().getEquippedForAppearance()), true);
		if (player.isInRobotMode()) {
			PacketSendUtility.broadcastPacket(player, new SM_RIDE_ROBOT(player, 0), true);
			PacketSendUtility.broadcastPacket(player, new SM_RIDE_ROBOT(player), true);
		}
	}

	public static int getRobotId(Player player) {
		if (!player.isInRobotMode())
			return player.getRobotId();
		Appearance appearance = appearanceByPlayerId.get(player.getObjectId());
		return appearance == null ? player.getRobotId() : appearance.robotId;
	}

	public static List<Item> computeItems(Player player) {
		Appearance appearance = appearanceByPlayerId.computeIfPresent(player.getObjectId(), (k, v) -> createAppearance(player));
		return appearance == null ? null : appearance.items;
	}

	private record Appearance(List<Item> items, int robotId) {}
}
