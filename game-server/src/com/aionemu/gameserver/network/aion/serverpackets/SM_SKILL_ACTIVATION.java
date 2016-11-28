package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_SKILL_ACTIVATION extends AionServerPacket {

	private boolean isActive;
	private int unk;
	private int skillId;

	/**
	 * For toggle skills
	 * 
	 * @param skillId
	 * @param isActive
	 */
	public SM_SKILL_ACTIVATION(int skillId, boolean isActive) {
		this.skillId = skillId;
		this.isActive = isActive;
		this.unk = 0;
	}

	/**
	 * For stigma remove should work in 1.5.1.15
	 * 
	 * @param skillId
	 */
	public SM_SKILL_ACTIVATION(int skillId) {
		this.skillId = skillId;
		this.isActive = true;
		this.unk = 1;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillId);
		writeD(unk);
		writeC(isActive ? 1 : 0);
	}
}
