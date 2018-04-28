package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.TuningAction;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.item.ItemActionService;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author xTz
 */
public class CM_TUNE extends AionClientPacket {

	private int itemObjectId, tuningScrollObjectId;

	public CM_TUNE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		itemObjectId = readD();
		tuningScrollObjectId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;

		Item item = player.getInventory().getItemByObjId(itemObjectId);
		if (item == null)
			return;

		if (!item.isIdentified()) {
			ItemActionService.identifyItem(player, item);
		} else if (tuningScrollObjectId != 0) {
			Item tuningScroll = player.getInventory().getItemByObjId(tuningScrollObjectId);
			if (tuningScroll == null)
				return;

			TuningAction action = tuningScroll.getItemTemplate().getActions().getTuningAction();
			if (action != null && action.canAct(player, tuningScroll, item))
				action.act(player, tuningScroll, item);
		} else {
			AuditLogger.log(player, "attempted to tune an already identified item without tuning scroll.");
		}
	}

}
