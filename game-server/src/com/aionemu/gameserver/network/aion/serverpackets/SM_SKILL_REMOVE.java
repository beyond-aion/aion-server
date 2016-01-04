package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 * @modified bobobear
 */
public class SM_SKILL_REMOVE extends AionServerPacket {

	private int skillId;
	private int skillLevel;
	private int skillType;

	// SkillType == 0 Normal Skills, 1 Stigma Skills, 3 Linked Stigma Skills
	public SM_SKILL_REMOVE(int skillId, int skillLevel, int skillType) {
		this.skillId = skillId;
		this.skillLevel = skillLevel;
		this.skillType = skillType;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillId);
		if (skillId >= 30001 && skillId <= 30003 || skillId >= 40001 && skillId <= 40010) {
			writeC(0);
			writeC(0);
		} else if (skillType > 0) {
			writeC(skillLevel);
			writeC(skillType); // 1 Stigma, 3 Linked Stigma
		} else { // remove skills active or passive
			writeC(skillLevel);
		}
	}
}
