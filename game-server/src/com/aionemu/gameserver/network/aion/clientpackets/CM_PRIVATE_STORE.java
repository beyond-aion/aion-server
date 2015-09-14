package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.trade.TradePSItem;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.PrivateStoreService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Simple
 */
public class CM_PRIVATE_STORE extends AionClientPacket {

	/**
	 * Private store information
	 */
	private Player activePlayer;
	private TradePSItem[] tradePSItems;
	private int itemCount;
	private boolean cancelStore;

	public CM_PRIVATE_STORE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		/**
		 * Define who wants to create a private store
		 */
		activePlayer = getConnection().getActivePlayer();
		if (activePlayer == null) {
			return;
		}
		if (activePlayer.isInPrison()) {
			cancelStore = true;
			PacketSendUtility.sendMessage(activePlayer, "You can't open Private Shop in prison!");
			return;
		}

		/**
		 * Read the amount of items that need to be put into the player's store
		 */
		itemCount = readH();
		tradePSItems = new TradePSItem[itemCount];

		if (activePlayer.getMoveController().isInMove()) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_PERSONAL_SHOP_DISABLED_IN_MOVING_OBJECT);
			cancelStore = true;
			return;
		}

		for (int i = 0; i < itemCount; i++) {
			int itemObjId = readD();
			int itemId = readD();
			int count = readH();
			long price = readQ();
			Item item = activePlayer.getInventory().getItemByObjId(itemObjId);
			if ((price < 0 || item == null || item.getItemId() != itemId || item.getItemCount() < count) && !cancelStore) {
				PacketSendUtility.sendMessage(activePlayer, "Invalid item.");
				cancelStore = true;
				return;
			} else if (item.getPackCount() <= 0 && !item.isTradeable(activePlayer)) {
				PacketSendUtility.sendPacket(activePlayer, new SM_SYSTEM_MESSAGE(1300344, new DescriptionId(item.getNameId())));
				cancelStore = true;
				return;
			}

			tradePSItems[i] = new TradePSItem(itemObjId, itemId, count, price);
		}
	}

	@Override
	protected void runImpl() {
		if (activePlayer == null) {
			return;
		}
		if (activePlayer.getLifeStats().isAlreadyDead()) {
			return;
		}

		if (activePlayer.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_PRIVATESTORE) {
			PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(activePlayer,
				"Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		if (!cancelStore && itemCount > 0) {
			PrivateStoreService.addItems(activePlayer, tradePSItems);
		} else {
			PrivateStoreService.closePrivateStore(activePlayer);
		}
	}
}
