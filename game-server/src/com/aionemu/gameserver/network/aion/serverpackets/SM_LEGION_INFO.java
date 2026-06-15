package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.abyss.AbyssRankingCache;

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
		writeD(AbyssRankingCache.getInstance().getRankingListPosition(legion));
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
		writeAnnouncements();
	}

	/**
	 * The game client expects up to 7 announcements, but it only shows the first one, so only one is sent. The code could be simplified with just one
	 * announcement, but this implementation is more accurate and future-proof.
	 */
	private void writeAnnouncements() {
		List<Legion.Announcement> announcements = Collections.singletonList(legion.getAnnouncement());
		for (int i = 0; i < 7; i++) {
			Legion.Announcement announcement = i < announcements.size() ? announcements.get(i) : null;
			writeS(announcement == null ? "" : announcement.message());
			if (announcement == null || announcement.message().isEmpty()) // empty string is a stop marker
				break;
			writeD((int) (announcement.time().getTime() / 1000));
		}
	}
}
