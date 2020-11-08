package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.skillinfo.SkillEntryWriter;

/**
 * @author MrPoke, ATracer, Neon
 */
public class SM_SKILL_LIST extends AionServerPacket {

	public static final int STATIC_BODY_SIZE = 7;

	private final List<PlayerSkillEntry> skillList;
	private final int messageId;
	private String skillNameL10n;
	private String skillLvl;
	boolean silentUpdate = false;

	public SM_SKILL_LIST(List<PlayerSkillEntry> skillList) {
		this.skillList = skillList;
		this.messageId = 0;
		this.silentUpdate = true;
	}

	public SM_SKILL_LIST(PlayerSkillEntry skill, int messageId) {
		this.skillList = Collections.singletonList(skill);
		this.messageId = messageId;
		this.skillNameL10n = DataManager.SKILL_DATA.getSkillTemplate(skill.getSkillId()).getL10n();
		this.skillLvl = String.valueOf(skill.getSkillLevel());
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillList.size()); // skills list size
		writeC(silentUpdate ? 1 : 0); // 1 only list in skill list, 0 list in skill list, update skill bar level and notify if new
		for (PlayerSkillEntry entry : skillList)
			SkillEntryWriter.writeSkillEntry(entry, getBuf());
		writeD(messageId);
		if (messageId != 0) {
			writeS(skillNameL10n);
			writeS(skillLvl);
			writeH(0x00);
		}
	}
}
