package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.item.ItemChargeService;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer
 */
public class CM_CHARGE_ITEM extends AionClientPacket {

	private int targetNpcObjectId;
	private int chargeLevel;
	private List<Integer> itemObjectIds;

	public CM_CHARGE_ITEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		targetNpcObjectId = readD();
		chargeLevel = readUC();
		int itemsSize = readUH();
		itemObjectIds = new ArrayList<>();
		for (int i = 0; i < itemsSize; i++)
			itemObjectIds.add(readD());
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!player.isTargetingNpcWithFunctionNearby(targetNpcObjectId, DialogAction.CHARGE_ITEM_MULTI, DialogAction.CHARGE_ITEM_SINGLE,
			DialogAction.CHARGE_ITEM_MULTI2, DialogAction.CHARGE_ITEM_SINGLE2)) {
			AuditLogger.log(player, "tried to charge items without targeting a conditioning or augmenting officer in range");
			return;
		}

		List<Item> itemsToCharge = new ArrayList<>();
		for (int itemObjId : itemObjectIds) {
			Item item = player.getInventory().getItemByObjId(itemObjId);
			if (item != null)
				itemsToCharge.add(item);
		}
		ItemChargeService.chargeItems(player, itemsToCharge, chargeLevel, false, true);
	}

}
