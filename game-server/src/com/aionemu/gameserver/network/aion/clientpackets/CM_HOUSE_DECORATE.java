package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.PartType;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_EDIT;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * @author Rolandas
 */
public class CM_HOUSE_DECORATE extends AionClientPacket {

	int objectId;
	int templateId;
	int lineNr; // Line number (starts from 1 in 3.0 and from 2 in 3.5) of part in House render/update packet

	public CM_HOUSE_DECORATE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
		templateId = readD();
		lineNr = readUH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;

		House house = player.getHouseRegistry().getOwner();

		PartType partType = PartType.getForLineNr(lineNr);
		int room = lineNr - partType.getStartLineNr();

		if (objectId == 0) {
			// change appearance to default, delete any applied customs finally
			HouseDecoration decor = house.getRegistry().getDefaultPartByType(partType, room);
			if (decor.isUsed()) {
				return;
			}
			house.getRegistry().setPartInUse(decor, room);
		} else {
			// remove from inventory
			HouseDecoration decor = house.getRegistry().getCustomPartByObjId(objectId);
			house.getRegistry().setPartInUse(decor, room);
			sendPacket(new SM_HOUSE_EDIT(4, 2, objectId)); // yes, in retail it's sent twice!
		}

		sendPacket(new SM_HOUSE_EDIT(4, 2, objectId));
		house.getRegistry().setPersistentState(PersistentState.UPDATE_REQUIRED);
		house.getController().updateAppearance();
		QuestEngine.getInstance().onHouseItemUseEvent(new QuestEnv(null, player, 0));
	}

}
