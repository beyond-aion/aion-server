package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Arrays;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author MrPoke
 * @modified ATracer, Neon
 */
public class SM_SKILL_LIST extends AionServerPacket {

	private List<PlayerSkillEntry> skillList;
	private int messageId;
	private int skillNameId;
	private String skillLvl;
	boolean silentUpdate = false;

	public SM_SKILL_LIST(List<PlayerSkillEntry> skillList) {
		this.skillList = skillList;
		this.messageId = 0;
		this.silentUpdate = true;
	}

	public SM_SKILL_LIST(PlayerSkillEntry skill, int messageId) {
		this.skillList = Arrays.asList(skill);
		this.messageId = messageId;
		this.skillNameId = DataManager.SKILL_DATA.getSkillTemplate(skill.getSkillId()).getNameId();
		this.skillLvl = String.valueOf(skill.getSkillLevel());
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillList.size()); // skills list size
		writeC(silentUpdate ? 1 : 0); // 1 only list in skill list, 0 list in skill list, update skill bar level and notify if new
		for (PlayerSkillEntry entry : skillList) {
			writeH(entry.getSkillId());// id
			writeH(entry.isNormalSkill() ? 1 : entry.getSkillLevel()); // don't ask me, it's retail like...
			writeC(0x00);
			writeC(entry.getProfessionSkillBarSize());
			writeD(entry.isProfessionSkill() ? entry.getProfessionFlag() : entry.getFlag());
			writeC(entry.getSkillType()); // 0 normal skill , 1 stigma skill , 3 linked stigma skill
		}
		writeD(messageId);
		if (messageId != 0) {
			writeNameId(skillNameId);
			writeS(skillLvl);
			writeH(0x00);
		}
	}
}
