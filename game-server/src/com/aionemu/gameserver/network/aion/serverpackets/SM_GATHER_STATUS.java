package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author orz
 * @modified Yeats
 */
public class SM_GATHER_STATUS extends AionServerPacket {

	private int status;
	private int action;
	private int playerobjid;
	private int gatherableobjid;
	private int skillId;

	/**
	 * 
	 * @param playerobjid
	 * @param gatherableobjid
	 * @param skillId
	 * @param action
	 */
	public SM_GATHER_STATUS(int playerobjid, int gatherableobjid, int skillId, int action) {// int status) {
		this.playerobjid = playerobjid;
		this.gatherableobjid = gatherableobjid;
		//this.status = status;
		this.action = action;
		this.skillId = skillId;
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	protected void writeImpl(AionConnection con) {

		writeD(playerobjid);
		writeD(gatherableobjid);
		//writeH(0); // unk
		//writeC(status);
		writeH(skillId);
		writeC(action);
	}
}
