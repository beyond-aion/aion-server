package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionEmblemType;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.LegionService;

/**
 * @author Simple, cura, Neon
 */
public class CM_LEGION_MODIFY_EMBLEM extends AionClientPacket {

	private int legionId;
	private int emblemId;
	private int alpha;
	private int red;
	private int green;
	private int blue;
	private LegionEmblemType emblemType;

	public CM_LEGION_MODIFY_EMBLEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		legionId = readD();
		emblemId = readUC();
		emblemType = (readUC() == LegionEmblemType.DEFAULT.getValue()) ? LegionEmblemType.DEFAULT : LegionEmblemType.CUSTOM;
		alpha = readUC();
		red = readUC();
		green = readUC();
		blue = readUC();
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		if (activePlayer.isLegionMember() && activePlayer.getLegion().getLegionId() == legionId)
			LegionService.getInstance().storeLegionEmblem(activePlayer, emblemId, alpha, red, green, blue, emblemType);
	}
}
