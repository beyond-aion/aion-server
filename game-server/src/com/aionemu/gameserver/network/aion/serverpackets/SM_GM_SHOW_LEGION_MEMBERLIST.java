package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.team.legion.LegionMemberEx;

/**
 * @author Yeats.
 */
public class SM_GM_SHOW_LEGION_MEMBERLIST extends SM_LEGION_MEMBERLIST {

	public SM_GM_SHOW_LEGION_MEMBERLIST(List<LegionMemberEx> legionMembers, boolean isFirst, boolean isLast) {
		super(legionMembers, isFirst, isLast);
	}

}
