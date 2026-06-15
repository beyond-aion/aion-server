package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple
 */
public class SM_LEGION_UPDATE_MEMBER extends AionServerPacket {

	private final LegionMember legionMember;
	private final int msgId;
	private final String text;

	public SM_LEGION_UPDATE_MEMBER(Player player, int msgId, String text) {
		this(player.getLegionMember(), msgId, text);
	}

	public SM_LEGION_UPDATE_MEMBER(LegionMember legionMember, int msgId, String text) {
		this.legionMember = legionMember;
		this.msgId = msgId;
		this.text = text;
	}

	public SM_LEGION_UPDATE_MEMBER(LegionMember legionMember) {
		this(legionMember, 0, null);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(legionMember.getObjectId());
		writeC(legionMember.getRank().getRankId());
		writeC(legionMember.getPlayerClass().getClassId());
		writeC(legionMember.getLevel());
		writeD(legionMember.getWorldId());
		writeC(legionMember.isOnline() ? 1 : 0);
		writeD(legionMember.isOnline() ? 0 : legionMember.getLastOnlineEpochSeconds());
		writeD(NetworkConfig.GAMESERVER_ID); // TODO: add to account model?
		writeD(msgId);
		writeS(text);
	}
}
