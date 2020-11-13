package com.aionemu.gameserver.network.aion.serverpackets;

import java.sql.Timestamp;
import java.util.Map;

import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple
 */
public class SM_LEGION_INFO extends AionServerPacket {

	private final Legion legion;

	public SM_LEGION_INFO(Legion legion) {
		this.legion = legion;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeS(legion.getName());
		writeC(legion.getLegionLevel());
		writeD(legion.getLegionRank());
		writeH(legion.getDeputyPermission());
		writeH(legion.getCenturionPermission());
		writeH(legion.getLegionaryPermission());
		writeH(legion.getVolunteerPermission());
		writeQ(legion.getContributionPoints());
		writeD(0x00); // unk
		writeD(0x00); // unk
		writeD(legion.getDisbandTime());
		writeD(legion.getOccupiedLegionDominion());
		writeD(legion.getLastLegionDominion());
		writeD(legion.getCurrentLegionDominion());
		Map<Timestamp, String> announcementList = legion.getAnnouncementList().descendingMap();
		// Show max 7 announcements
		int i = 0;
		for (Timestamp unixTime : announcementList.keySet()) {
			writeS(announcementList.get(unixTime));
			writeD((int) (unixTime.getTime() / 1000));
			if (++i >= 7)
				break;
		}
	}
}
