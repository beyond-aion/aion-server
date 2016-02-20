package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionEmblemType;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.LegionService;

/**
 * @author Simple modified cura
 */
public class CM_LEGION_UPLOAD_INFO extends AionClientPacket {

	/** Emblem related information **/
	private int totalSize;
	private int alpha;
	private int red;
	private int green;
	private int blue;

	/**
	 * @param opcode
	 */
	public CM_LEGION_UPLOAD_INFO(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		totalSize = readD();
		alpha = readC();
		red = readC();
		green = readC();
		blue = readC();
	}

	@Override
	protected void runImpl() {
		final Player activePlayer = getConnection().getActivePlayer();
		LegionService.getInstance().uploadEmblemInfo(activePlayer, totalSize, alpha, red, green, blue, LegionEmblemType.CUSTOM);
	}
}
