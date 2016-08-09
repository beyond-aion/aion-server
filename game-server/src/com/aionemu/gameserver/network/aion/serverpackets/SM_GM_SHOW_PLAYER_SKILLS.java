package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Yeats
 */
public class SM_GM_SHOW_PLAYER_SKILLS extends AionServerPacket {

	List<PlayerSkillEntry> skillList;
	public SM_GM_SHOW_PLAYER_SKILLS(List<PlayerSkillEntry> skillList) {
		this.skillList = skillList;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillList.size()); // skills list size
		for (PlayerSkillEntry entry : skillList) {
			writeH(entry.getSkillId());// id
			writeH(entry.isNormalSkill() ? 1 : entry.getSkillLevel()); // don't ask me, it's retail like...
			writeC(0x00);
			writeC(entry.getProfessionSkillBarSize());
			writeD(entry.isProfessionSkill() ? entry.getProfessionFlag() : entry.getFlag());
			writeC(entry.getSkillType()); // 0 normal skill , 1 stigma skill , 3 linked stigma skill
		}
	}
}
