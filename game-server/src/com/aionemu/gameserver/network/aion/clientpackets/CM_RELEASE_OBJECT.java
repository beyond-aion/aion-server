package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.concurrent.Future;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.PostboxObject;
import com.aionemu.gameserver.model.gameobjects.UseableHouseObject;
import com.aionemu.gameserver.model.gameobjects.UseableItemObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 * @modified Neon
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
		VisibleObject object = player.getKnownList().getObject(targetObjectId);
		if (object instanceof UseableHouseObject<?> && ((UseableHouseObject<?>) object).releaseOccupant(player)) { // release object
			Future<?> task = player.getController().getTask(TaskId.HOUSE_OBJECT_USE);
			if (task != null && !task.isDone() || object instanceof PostboxObject) { // post box always sends the message
				if (object instanceof UseableItemObject) // reset visual use progress bar
					PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), object.getObjectId(), 0, 9));
				sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_CANCEL_USE());
			}
			player.getController().cancelTask(TaskId.HOUSE_OBJECT_USE);
		}
	}
}
