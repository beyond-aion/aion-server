package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.model.legionDominion.LegionDominionParticipantInfo;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.LegionDominionService;

/**
 * @author Yeats
 *
 */
public class SM_LEGION_DOMINION_RANK extends AionServerPacket {

	private int id;
	
	public SM_LEGION_DOMINION_RANK(int id) {
		this.id = id;
	}
	
	@Override
	protected void writeImpl(AionConnection con) {
		LegionDominionLocation loc = LegionDominionService.getInstance().getLegionDominionLoc(id);
		if (loc != null) {
			writeD(id);
			Legion legion = con.getActivePlayer().getLegion();
			if (legion != null && loc.getParticipantInfo().containsKey(legion.getLegionId()) && loc.getParticipantInfo().get(legion.getLegionId()).getPoints() > 0) {
				LegionDominionParticipantInfo curLegion = loc.getParticipantInfo().get(legion.getLegionId());
				List<LegionDominionParticipantInfo> pInfo = loc.getSortedTop25Participants(curLegion);
				writeC(pInfo.indexOf(curLegion) + 1);
				writeH(loc.getParticipantInfo().size());
				for (LegionDominionParticipantInfo info : pInfo) {
					writeD(info.getPoints());
					writeD(info.getTime());
					writeQ(info.getDate());
					writeS(info.getLegionName());
				}
			} else {
				writeC(0);
				writeH(0);
			}
		}
	}
}
