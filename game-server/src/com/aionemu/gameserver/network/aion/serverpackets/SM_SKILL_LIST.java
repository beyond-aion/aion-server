package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import javolution.util.FastTable;

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
	boolean isNew = false;

	/**
	 * This constructor is used on player entering the world Constructs new <tt>SM_SKILL_LIST</tt> packet
	 */
	public SM_SKILL_LIST(PlayerSkillEntry skillListEntry) {
		this(FastTable.of(skillListEntry));
	}

	public SM_SKILL_LIST(List<PlayerSkillEntry> skillList) {
		this.skillList = skillList;
		this.messageId = 0;
	}

	public SM_SKILL_LIST(PlayerSkillEntry skillListEntry, int messageId, boolean isNew) {
		this.skillList = FastTable.of(skillListEntry);
		this.messageId = messageId;
		this.skillNameId = DataManager.SKILL_DATA.getSkillTemplate(skillListEntry.getSkillId()).getNameId();
		this.skillLvl = String.valueOf(skillListEntry.getSkillLevel());
		this.isNew = isNew;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillList.size()); // skills list size
		writeC(isNew ? 0 : 1); // 1 all learned skills, 0 newly updated skills
		for (PlayerSkillEntry entry : skillList) {
			writeH(entry.getSkillId());// id
			writeH(entry.getSkillLevel());// lvl
			writeC(0x00);
			writeC(entry.getProfessionSkillBarSize());
			writeD(entry.isProfessionSkill() ? entry.getProfessionFlag() : entry.getFlag(isNew));
			writeC(entry.isStigma() ? 1 : 0); // stigma
		}
		writeD(messageId);
		if (messageId != 0) {
			writeH(0x24); // unk
			writeD(skillNameId);
			writeH(0x00);
			writeS(skillLvl);
		}
	}
}
