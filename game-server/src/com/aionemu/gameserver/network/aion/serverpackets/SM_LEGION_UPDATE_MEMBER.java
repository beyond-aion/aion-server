package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.LegionMemberEx;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple
 */
public class SM_LEGION_UPDATE_MEMBER extends AionServerPacket {

	private Player player;
	private LegionMemberEx LM;
	private int msgId;
	private String text;

	public SM_LEGION_UPDATE_MEMBER(Player player, int msgId, String text) {
		this.player = player;
		this.msgId = msgId;
		this.text = text;
	}

	public SM_LEGION_UPDATE_MEMBER(LegionMemberEx LM, int msgId, String text) {
		this.LM = LM;
		this.msgId = msgId;
		this.text = text;
	}

	public SM_LEGION_UPDATE_MEMBER(Player player) {
		this.player = player;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (player != null) {
			writeD(player.getObjectId());
			writeC(player.getLegionMember().getRank().getRankId());
			writeC(player.getCommonData().getPlayerClass().getClassId());
			writeC(player.getLevel());
			writeD(player.getPosition().getMapId());
			writeC(player.isOnline() ? 1 : 0);
			writeD(player.isOnline() ? 0 : player.getCommonData().getLastOnlineEpochSeconds());
			writeD(NetworkConfig.GAMESERVER_ID); // TODO: add to account model?
			writeD(msgId);
			writeS(text);
		} else if (LM != null) {
			writeD(LM.getObjectId());
			writeC(LM.getRank().getRankId());
			writeC(LM.getPlayerClass().getClassId());
			writeC(LM.getLevel());
			writeD(LM.getWorldId());
			writeC(LM.isOnline() ? 1 : 0);
			writeD(LM.isOnline() ? 0 : LM.getLastOnlineEpochSeconds());
			writeD(NetworkConfig.GAMESERVER_ID); // TODO: add to account model?
			writeD(msgId);
			writeS(text);
		}
	}
}
