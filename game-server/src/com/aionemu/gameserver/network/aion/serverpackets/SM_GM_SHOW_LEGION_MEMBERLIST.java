package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.team.legion.LegionMemberEx;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.HousingService;

/**
 * @author Yeats.
 */
public class SM_GM_SHOW_LEGION_MEMBERLIST extends AionServerPacket {

	private boolean isFirst, isSplit;
	private List<LegionMemberEx> legionMembers;

	public SM_GM_SHOW_LEGION_MEMBERLIST(List<LegionMemberEx> legionMembers, boolean isSplit, boolean isFirst) {
		this.legionMembers = legionMembers;
		this.isSplit = isSplit;
		this.isFirst = isFirst;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		int size = legionMembers.size();
		writeC(isFirst ? 1 : 0);
		writeH(isSplit ? size : -size);
		for (LegionMemberEx legionMember : legionMembers) {
			writeD(legionMember.getObjectId());
			writeS(legionMember.getName());
			writeH(legionMember.getPlayerClass().getClassId());
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
			writeD(NetworkConfig.GAMESERVER_ID); // TODO: add to account model? displays server number for each away player in region field
		}
	}
}
