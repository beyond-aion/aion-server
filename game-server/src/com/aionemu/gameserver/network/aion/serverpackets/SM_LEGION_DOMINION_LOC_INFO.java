package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.model.team.legion.LegionEmblem;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.LegionDominionService;
import com.aionemu.gameserver.services.LegionService;

/**
 * @author Neon, Yeats
 */
public class SM_LEGION_DOMINION_LOC_INFO extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(6);
		for (LegionDominionLocation loc : LegionDominionService.getInstance().getLegionDominions()) {
			int legionId = loc.getLegionId();
			String name = "";
			LegionEmblem emblem = new LegionEmblem();
			if (legionId != 0 && LegionService.getInstance().getLegion(legionId) != null) {
				emblem = LegionService.getInstance().getLegion(legionId).getLegionEmblem();
				name = LegionService.getInstance().getLegion(legionId).getName();
			}
			writeD(loc.getLocationId());
			writeD(legionId);
			writeC(emblem.getEmblemId());
			writeC(emblem.getEmblemType().getValue());
			writeC(emblem.getColor_a());
			writeC(emblem.getColor_r());
			writeC(emblem.getColor_g());
			writeC(emblem.getColor_b());
			writeS(name);
		}
	}
}
