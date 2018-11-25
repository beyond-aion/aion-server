package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.assemblednpc.AssembledNpc;
import com.aionemu.gameserver.model.assemblednpc.AssembledNpcPart;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_NPC_ASSEMBLER extends AionServerPacket {

	private AssembledNpc assembledNpc;
	private int routeId;
	private long timeOnMap;

	public SM_NPC_ASSEMBLER(AssembledNpc assembledNpc) {
		this.assembledNpc = assembledNpc;
		if (assembledNpc != null) {
			this.routeId = assembledNpc.getRouteId();
			timeOnMap = assembledNpc.getTimeOnMap();
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (assembledNpc != null) {
			writeD(assembledNpc.getAssembledParts().size()); // size
			for (AssembledNpcPart npc : assembledNpc.getAssembledParts()) {
				writeD(routeId); // routeId
				writeD(npc.getObject()); // objectId
				writeD(npc.getNpcId()); // npc Id
				writeD(npc.getStaticId()); // static Id
				writeQ(timeOnMap); // time
			}
		} else {
			writeD(0);
		}
	}
}
