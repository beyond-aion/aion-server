package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionEmblemType;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.LegionService;

/**
 * @author Simple, cura
 */
public class CM_LEGION_UPLOAD_INFO extends AionClientPacket {

	/** Emblem related information **/
	private int totalSize;
	private int alpha;
	private int red;
	private int green;
	private int blue;

	public CM_LEGION_UPLOAD_INFO(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		totalSize = readD();
		alpha = readUC();
		red = readUC();
		green = readUC();
		blue = readUC();
	}

	@Override
	protected void runImpl() {
		final Player activePlayer = getConnection().getActivePlayer();
		LegionService.getInstance().uploadEmblemInfo(activePlayer, totalSize, alpha, red, green, blue, LegionEmblemType.CUSTOM);
	}
}
