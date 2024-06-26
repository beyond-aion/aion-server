package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.team.legion.LegionMemberEx;
import com.aionemu.gameserver.services.player.PlayerService;

/**
 * @author Yeats
 */
public class SM_GM_SHOW_LEGION_MEMBERLIST extends SM_LEGION_MEMBERLIST {

	public SM_GM_SHOW_LEGION_MEMBERLIST(List<LegionMemberEx> legionMembers, boolean isFirst, boolean isLast) {
		super(legionMembers, isFirst, isLast);
	}

	@Override
	protected void writeLegionMember(LegionMemberEx legionMember) {
		writeD(legionMember.getObjectId());
		writeS(legionMember.getName());
		writeC(legionMember.getPlayerClass().getClassId());
		writeC(PlayerService.getOrLoadPlayerCommonData(legionMember.getObjectId()).getGender().getGenderId());
		writeD(legionMember.getLevel());
		writeC(legionMember.getRank().getRankId());
		writeD(legionMember.getWorldId());
		writeC(legionMember.isOnline() ? 1 : 0);
		writeS(legionMember.getSelfIntro());
		writeS(legionMember.getNickname());
		writeD(legionMember.isOnline() ? 0 : legionMember.getLastOnlineEpochSeconds());
	}
}
