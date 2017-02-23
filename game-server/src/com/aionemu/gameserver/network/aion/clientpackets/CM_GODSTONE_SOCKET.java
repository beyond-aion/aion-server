package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.item.ItemSocketService;

/**
 * @author ATracer
 */
public class CM_GODSTONE_SOCKET extends AionClientPacket {

	private int npcObjectId;
	private int weaponId;
	private int stoneId;

	public CM_GODSTONE_SOCKET(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		this.npcObjectId = readD();
		this.weaponId = readD();
		this.stoneId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player.getTarget() instanceof Npc && player.getTarget().getObjectId() == npcObjectId && player.getDistanceToTarget() <= 7)
			ItemSocketService.socketGodstone(player, player.getEquipment().getEquippedItemByObjId(weaponId), stoneId);
	}
}
