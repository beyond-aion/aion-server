package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Simple
 */
public class CM_LEGION_UPLOAD_EMBLEM extends AionClientPacket {

	private byte[] data;

	public CM_LEGION_UPLOAD_EMBLEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		data = readB(readD());
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!player.isLegionMember()) {
			AuditLogger.log(player, "uploaded a legion emblem without being member of a legion");
			return;
		}

		if (data != null && data.length > 0) {
			LegionService.getInstance().uploadEmblemData(player, data);
		}
	}
}
