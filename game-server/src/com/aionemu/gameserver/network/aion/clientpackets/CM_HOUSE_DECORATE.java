package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HousingCommand;
import com.aionemu.gameserver.model.house.HousingContainer;
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

	private int objectId;
	private int lineNo, roomNo; // Line number (starts from 1 in 3.0 and from 2 in 3.5) of part in House render/update packet

	public CM_HOUSE_DECORATE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		objectId = readD();
		readD(); // templateId (already known by objectId)
		lineNo = readUH();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;

		House house = player.getActiveHouse();
		if (house == null || !house.isOnLand(player))
			return;

		PartType partType = PartType.getForLineNr(lineNo);
		if (partType == null) // client may send lineNos which are not even implemented on client side (like 20-26)
			return;
		int roomNo = lineNo - partType.getStartLineNr();

		if (objectId == 0) { // change appearance to default and delete any applied custom decor
			house.getRegistry().discardDecor(partType, roomNo);
		} else { // apply decor and remove it from registry
			HouseDecoration decor = house.getRegistry().getDecorByObjId(objectId);
			if (decor == null)
				return;
			house.getRegistry().setUsed(decor, roomNo);
			sendPacket(new SM_HOUSE_EDIT(HousingCommand.REMOVE_ITEM, HousingContainer.DECORATIONS, objectId)); // yes, in retail it's sent twice!
		}

		sendPacket(new SM_HOUSE_EDIT(HousingCommand.REMOVE_ITEM, HousingContainer.DECORATIONS, objectId));
		house.getController().updateAppearance();
		QuestEngine.getInstance().onHouseItemUseEvent(new QuestEnv(null, player, 0));
	}

}
