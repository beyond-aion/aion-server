package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.team.legion.LegionMemberEx;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.HousingService;

/**
 * @author Simple
 */
public class SM_LEGION_MEMBERLIST extends AionServerPacket {

	private final boolean isFirst, isLast;
	private final List<LegionMemberEx> legionMembers;

	public SM_LEGION_MEMBERLIST(List<LegionMemberEx> legionMembers, boolean isFirst, boolean isLast) {
		this.legionMembers = legionMembers;
		this.isFirst = isFirst;
		this.isLast = isLast;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		int size = legionMembers.size();
		writeC(isFirst ? 1 : 0);
		writeH(isLast ? -size : size);
		for (LegionMemberEx legionMember : legionMembers)
			writeLegionMember(legionMember);
	}

	protected void writeLegionMember(LegionMemberEx legionMember) {
		writeD(legionMember.getObjectId());
		writeS(legionMember.getName());
		writeC(legionMember.getPlayerClass().getClassId());
		writeD(legionMember.getLevel());
		writeC(legionMember.getRank().getRankId());
		writeD(legionMember.getWorldId());
		writeC(legionMember.isOnline() ? 1 : 0);
		writeS(legionMember.getSelfIntro());
		writeS(legionMember.getNickname());
		writeD(legionMember.isOnline() ? 0 : legionMember.getLastOnlineEpochSeconds());
		House house = HousingService.getInstance().findActiveHouse(legionMember.getObjectId());
		writeD(house == null ? 0 : house.getAddress().getId());
		writeD(house == null ? 0 : house.getDoorState().getId());
		writeD(NetworkConfig.GAMESERVER_ID); // displays server number for each away player in region field
	}
}
