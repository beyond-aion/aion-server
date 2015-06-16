package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * In this packet Server is sending Skill Info?
 * 
 * @author modified by ATracer,MrPoke
 */
public class SM_SKILL_LIST extends AionServerPacket {

	private PlayerSkillEntry[] skillList;
	private int messageId;
	private int skillNameId;
	private String skillLvl;
	public static final int YOU_LEARNED_SKILL = 1300050;
	boolean isNew = false;

	/**
	 * This constructor is used on player entering the world Constructs new <tt>SM_SKILL_LIST </tt> packet
	 */
	public SM_SKILL_LIST(Player player, PlayerSkillEntry[] basicSkills) {
		this.skillList = player.getSkillList().getBasicSkills();
		this.messageId = 0;
	}

	public SM_SKILL_LIST(Player player, PlayerSkillEntry stigmaSkill) {
		this.skillList = new PlayerSkillEntry[] { stigmaSkill };
		this.messageId = 0;
	}

	public SM_SKILL_LIST(PlayerSkillEntry skillListEntry, int messageId, boolean isNew) {
		this.skillList = new PlayerSkillEntry[] { skillListEntry };
		this.messageId = messageId;
		this.skillNameId = DataManager.SKILL_DATA.getSkillTemplate(skillListEntry.getSkillId()).getNameId();
		this.skillLvl = String.valueOf(skillListEntry.getSkillLevel());
		this.isNew = isNew;
	}

	@Override
	protected void writeImpl(AionConnection con) {

		final int size = skillList.length;
		writeH(size); // skills list size
		writeC(isNew ? 0 : 1); // 1 all learned skills, 0 skills can be learned ?

		if (size > 0) {
			for (PlayerSkillEntry entry : skillList) {
				writeH(entry.getSkillId());// id
				writeH(entry.getSkillLevel());// lvl
				writeC(0x00);
				int extraLevel = entry.getExtraLvl();
				writeC(extraLevel);
				if (isNew && extraLevel == 0 && !entry.isStigma())
					writeD((int) (System.currentTimeMillis() / 1000)); // Learned date NCSoft......
				else
					writeD(0);
				writeC(entry.isStigma() ? 1 : 0); // stigma
			}
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
