package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ginho1
 */
public class CM_CHECK_PAK extends AionClientPacket {

	@SuppressWarnings("unused")
	private byte unk;
	private String pakStatus;

	public CM_CHECK_PAK(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		unk = readC(); // 2
		pakStatus = readS();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!pakStatus.endsWith("[1:OK]") && !pakStatus.contains("File not found"))
			AuditLogger.info(player, "Player using modified data pak: " + pakStatus);
	}
}
