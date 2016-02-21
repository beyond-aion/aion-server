package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionEmblemType;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.LegionService;

/**
 * @author Simple
 * @modified cura, Neon
 */
public class CM_LEGION_MODIFY_EMBLEM extends AionClientPacket {

	private int legionId;
	private int emblemId;
	private int alpha;
	private int red;
	private int green;
	private int blue;
	private LegionEmblemType emblemType;

	/**
	 * @param opcode
	 */
	public CM_LEGION_MODIFY_EMBLEM(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		legionId = readD();
		emblemId = readC();
		emblemType = (readC() == LegionEmblemType.DEFAULT.getValue()) ? LegionEmblemType.DEFAULT : LegionEmblemType.CUSTOM;
		alpha = readC();
		red = readC();
		green = readC();
		blue = readC();
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		if (activePlayer.isLegionMember() && activePlayer.getLegion().getLegionId() == legionId)
			LegionService.getInstance().storeLegionEmblem(activePlayer, emblemId, alpha, red, green, blue, emblemType);
	}
}
