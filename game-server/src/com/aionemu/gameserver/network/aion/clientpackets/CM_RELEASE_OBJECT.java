package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.UseableItemObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 */
public class CM_RELEASE_OBJECT extends AionClientPacket {

	int targetObjectId;

	public CM_RELEASE_OBJECT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		targetObjectId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;
		if (player.getController().hasTask(TaskId.HOUSE_OBJECT_USE)) {
			VisibleObject object = World.getInstance().findVisibleObject(targetObjectId);
			if (object instanceof UseableItemObject && !player.getController().hasScheduledTask(TaskId.HOUSE_OBJECT_USE)) {
				// not cancelled
			} else {
				// mailboxes always show this message even if not cancelled
				player.getController().cancelTask(TaskId.HOUSE_OBJECT_USE);
				sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_CANCEL_USE);
			}
		}
	}

}
