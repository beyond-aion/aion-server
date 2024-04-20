package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz, Neon
 */
public class SM_SKILL_REMOVE extends AionServerPacket {

	private int skillId;
	private int skillLevel;
	private int skillType;

	public SM_SKILL_REMOVE(PlayerSkillEntry skill) {
		this.skillId = skill.getSkillId();
		this.skillLevel = skill.isProfessionSkill() ? skill.getProfessionFlag() : skill.getSkillLevel();
		this.skillType = skill.getSkillType();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillId);
		writeC(skillLevel); // for professions, the getProfessionFlag() sent in SM_SKILL_LIST value is relevant, otherwise client won't remove it...
		writeC(skillType);
	}
}
