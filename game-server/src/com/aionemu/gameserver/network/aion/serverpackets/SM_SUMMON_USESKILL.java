package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_SUMMON_USESKILL extends AionServerPacket {

	private int summonId;
	private int skillId;
	private int skillLvl;
	private int targetId;

	/**
	 * @param summonId
	 * @param skillId
	 * @param skillLvl
	 * @param targetId
	 */
	public SM_SUMMON_USESKILL(int summonId, int skillId, int skillLvl, int targetId) {
		this.summonId = summonId;
		this.skillId = skillId;
		this.skillLvl = skillLvl;
		this.targetId = targetId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(summonId);
		writeH(skillId);
		writeC(skillLvl);
		writeD(targetId);
	}

}
