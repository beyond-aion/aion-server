package com.aionemu.gameserver.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.gameobjects.player.QuestStateList;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.templates.StorageExpansionTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WAREHOUSE_INFO;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Simple, Luzien
 */
public class WarehouseService {

	private static final Logger log = LoggerFactory.getLogger(WarehouseService.class);
	private static final int MAX_EXPAND = 11;

	/**
	 * Shows Question window and expands on positive response
	 */
	public static void expandWarehouse(Player player, Npc npc) {
		StorageExpansionTemplate template = DataManager.WAREHOUSEEXPANDER_DATA.getWarehouseExpansionTemplate(npc.getNpcId());
		if (template == null) {
			log.warn("Warehouse expansion template could not be found for " + npc);
			return;
		}

		if (!canExpand(player))
			return;
		int newNpcExpansions = player.getWhNpcExpands() + 1;
		int minExpansionLevel = template.getMinExpansionLevel();
		if (newNpcExpansions < minExpansionLevel) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXTEND_CHAR_WAREHOUSE_CANT_EXTEND_DUE_TO_MINIMUM_EXTEND_LEVEL_BY_THIS_NPC(npc.getObjectTemplate().getL10n(), minExpansionLevel - 1));
			return;
		}
		Integer price = template.getPrice(newNpcExpansions);
		if (price == null || newNpcExpansions > template.getMaxExpansionLevel()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXTEND_CHAR_WAREHOUSE_CANT_EXTEND_MORE_DUE_TO_MAXIMUM_EXTEND_LEVEL_BY_THIS_NPC(npc.getObjectTemplate().getL10n(), template.getMaxExpansionLevel()));
			return;
		}
		RequestResponseHandler<Npc> responseHandler = new RequestResponseHandler<>(npc) {

			@Override
			public void acceptRequest(Npc requester, Player responder) {
				if (responder.getInventory().tryDecreaseKinah(price))
					expand(responder, true);
				else
					PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_WAREHOUSE_EXPAND_NOT_ENOUGH_MONEY()); // warehouse and cube use the same msg..
			}

		};

		boolean result = player.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_WAREHOUSE_EXPAND_WARNING, responseHandler);
		if (result) {
			PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_WAREHOUSE_EXPAND_WARNING, 0, 0, String.valueOf(price)));
		}
	}

	public static void expand(Player player, boolean isNpcExpand) {
		if (!canExpand(player))
			return;
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXTEND_CHAR_WAREHOUSE_SIZE_EXTENDED(8)); // 8 Slots added
		PlayerCommonData pcd = player.getCommonData();
		if (isNpcExpand) {
			pcd.setWhNpcExpands(pcd.getWhNpcExpands() + 1);
		} else {
			pcd.setWhBonusExpands(pcd.getWhBonusExpands() + 1);
		}
		player.setWarehouseLimit();

		sendWarehouseInfo(player, false);
	}

	public static boolean canExpandByTicket(Player player, int ticketLevel) {
		if (!canExpand(player))
			return false;
		if (player.getWhBonusExpands() - getCompletedWhQuests(player) >= ticketLevel) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXTEND_CHAR_WAREHOUSE_CANT_EXTEND_MORE());
			return false;
		}
		return true;
	}

	public static boolean canExpand(Player player) {
		int newExpansions = player.getWarehouseExpansions() + 1;
		if (newExpansions < 0)
			return false;
		if (newExpansions > MAX_EXPAND) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXTEND_CHAR_WAREHOUSE_CANT_EXTEND_MORE());
			return false;
		}
		return true;
	}

	private static int getCompletedWhQuests(Player player) {
		int result = 0;
		QuestStateList qs = player.getQuestStateList();
		int[] questIds = { 1987, 2985 };
		for (int q : questIds) {
			if (qs.getQuestState(q) != null && qs.getQuestState(q).getStatus().equals(QuestStatus.COMPLETE))
				result++;
		}
		return result;
	}

	/**
	 * Sends correctly warehouse packets
	 *
	 * @param player
	 */
	public static void sendWarehouseInfo(Player player, boolean sendAccountWh) {
		List<Item> items = player.getStorage(StorageType.REGULAR_WAREHOUSE.getId()).getItems();

		int whSize = player.getWarehouseExpansions();
		int itemsSize = items.size();

		// regular warehouse
		boolean firstPacket = true;
		if (itemsSize != 0) {
			int index = 0;

			while (index + 10 < itemsSize) {
				PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(items.subList(index, index + 10), StorageType.REGULAR_WAREHOUSE.getId(), whSize,
					firstPacket, player));
				index += 10;
				firstPacket = false;
			}
			PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(items.subList(index, itemsSize), StorageType.REGULAR_WAREHOUSE.getId(), whSize,
				firstPacket, player));
		}

		PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(null, StorageType.REGULAR_WAREHOUSE.getId(), whSize, false, player));

		if (sendAccountWh) {
			// account warehouse
			PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(player.getStorage(StorageType.ACCOUNT_WAREHOUSE.getId()).getItemsWithKinah(),
				StorageType.ACCOUNT_WAREHOUSE.getId(), 0, true, player));
		}

		PacketSendUtility.sendPacket(player, new SM_WAREHOUSE_INFO(null, StorageType.ACCOUNT_WAREHOUSE.getId(), 0, false, player));
	}
}
