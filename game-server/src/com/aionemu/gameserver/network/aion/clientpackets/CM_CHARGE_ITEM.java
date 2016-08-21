package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Collection;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.item.ItemChargeService;

import javolution.util.FastTable;

/**
 * @author ATracer
 */
public class CM_CHARGE_ITEM extends AionClientPacket {

	private int targetNpcObjectId;
	private int chargeLevel;
	private Collection<Integer> itemIds;

	public CM_CHARGE_ITEM(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		targetNpcObjectId = readD();
		chargeLevel = readC();
		int itemsSize = readH();
		itemIds = new FastTable<>();
		for (int i = 0; i < itemsSize; i++) {
			itemIds.add(readD());
		}

	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!player.isTargeting(targetNpcObjectId)) {
			return; // TODO audit?
		}

		for (int itemObjId : itemIds) {
			Item item = player.getInventory().getItemByObjId(itemObjId);
			if (item != null) {
				int itemChargeLevel = item.getAvailableChargeLevel(player);
				int possibleChargeLevel = Math.min(itemChargeLevel, chargeLevel);
				if (possibleChargeLevel > 0) {
					if (ItemChargeService.processPayment(player, item, possibleChargeLevel))
						ItemChargeService.chargeItem(player, item, possibleChargeLevel);
				}
			}
		}
	}

}
