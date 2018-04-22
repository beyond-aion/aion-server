package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.item.ItemSocketService;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author ATracer
 */
public class CM_GODSTONE_SOCKET extends AionClientPacket {

	private int npcObjectId;
	private int weaponId;
	private int stoneId;

	public CM_GODSTONE_SOCKET(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		npcObjectId = readD();
		weaponId = readD();
		stoneId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		VisibleObject npc = player.getTarget();
		if (npc instanceof Npc && npc.getObjectId() == npcObjectId && PositionUtil.isInTalkRange(player, (Npc) npc))
			ItemSocketService.socketGodstone(player, player.getEquipment().getEquippedItemByObjId(weaponId), stoneId);
	}
}
