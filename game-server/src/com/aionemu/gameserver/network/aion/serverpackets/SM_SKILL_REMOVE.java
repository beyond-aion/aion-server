package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_SKILL_REMOVE extends AionServerPacket {

	private int skillId;
	private int skillLevel;
	private boolean isStigma;

	public SM_SKILL_REMOVE(int skillId, int skillLevel, boolean isStigma) {
		this.skillId = skillId;
		this.skillLevel = skillLevel;
		this.isStigma = isStigma;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillId);
		if (skillId >= 30001 && skillId <= 30003 || skillId >= 40001 && skillId <= 40010) {
			writeC(0);
			writeC(0);
		}
		else if (isStigma) {
			writeC(1);
			writeC(1);
		}
		else { // remove skills active or passive
			writeC(skillLevel);
		}
	}
}