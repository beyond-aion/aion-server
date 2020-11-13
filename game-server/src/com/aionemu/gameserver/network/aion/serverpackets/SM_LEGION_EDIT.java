package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.abyss.AbyssRankingCache;

/**
 * @author Simple
 */
public class SM_LEGION_EDIT extends AionServerPacket {

	private int type;
	private Legion legion;
	private int unixTime;
	private String announcement;

	public SM_LEGION_EDIT(int type) {
		this.type = type;
	}

	public SM_LEGION_EDIT(int type, Legion legion) {
		this.type = type;
		this.legion = legion;
	}

	public SM_LEGION_EDIT(int type, int unixTime) {
		this.type = type;
		this.unixTime = unixTime;
	}

	public SM_LEGION_EDIT(int type, int unixTime, String announcement) {
		this.type = type;
		this.announcement = announcement;
		this.unixTime = unixTime;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(type);
		switch (type) {
			case 0x00: // Change Legion Level
				writeC(legion.getLegionLevel());
				break;
			case 0x01: // Change Abyss Ranking List Position
				writeD(AbyssRankingCache.getInstance().getRankingListPosition(legion));
				break;
			case 0x02: // Change Legion Permissions
				writeH(legion.getDeputyPermission());
				writeH(legion.getCenturionPermission());
				writeH(legion.getLegionaryPermission());
				writeH(legion.getVolunteerPermission());
				break;
			case 0x03: // Change Legion Contributions
				writeQ(legion.getContributionPoints());
				break;
			case 0x04:
				writeQ(legion.getLegionWarehouse().getKinah());
				break;
			case 0x05: // Change Legion Announcement
				writeS(announcement);
				writeD(unixTime);
				break;
			case 0x06: // Disband Legion
				writeD(unixTime);
				break;
			case 0x07: // Recover Legion
				break;
			case 0x08: // Refresh Legion Announcement?
				break;
		}
	}
}
