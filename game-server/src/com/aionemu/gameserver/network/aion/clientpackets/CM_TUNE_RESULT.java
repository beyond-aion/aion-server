package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemActionService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Estrayl
 */
public class CM_TUNE_RESULT extends AionClientPacket {

	private int itemObjectId;
	private boolean hasAccepted;

	public CM_TUNE_RESULT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		itemObjectId = readD();
		hasAccepted = readC() == 1;
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Item itemToTune = player.getInventory().getItemByObjId(itemObjectId);
		if (itemToTune != null) {
			if (hasAccepted) {
				ItemActionService.applyTuneResult(player, itemToTune);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_REIDENTIFY_APPLY_YES(itemToTune.getL10n()));
			} else {
				itemToTune.setPendingTuneResult(null);
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ITEM_REIDENTIFY_APPLY_NO());
			}
			PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE_ITEM(player, itemToTune));
		}
	}

}
