package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer, MrPoke
 * @modified Neon, bobobear
 */
public class SM_SKILL_LIST extends AionServerPacket {

	private List<PlayerSkillEntry> skillList;
	private int messageId;
	private int skillNameId;
	private String skillLvl;
	private String lvlMsg;
	private boolean isNew = false;
	private boolean isAutolearn = false;

	/**
	 * This constructor is used on player entering the world Constructs new <tt>SM_SKILL_LIST</tt> packet
	 */
	public SM_SKILL_LIST(PlayerSkillEntry skill) {
		this.skillList = new FastTable<>();
		this.skillList.add(skill);
		this.messageId = 0;
		this.isAutolearn = true;
	}

	public SM_SKILL_LIST(List<PlayerSkillEntry> skillList) {
		this.skillList = skillList;
		this.messageId = 0;
		this.isAutolearn = false;
	}

	public SM_SKILL_LIST(PlayerSkillEntry skillListEntry, int messageId, String lvlMsg, boolean isAutolearn, boolean isNew) {
		this.skillList = new FastTable<>();
		this.skillList.add(skillListEntry);
		this.messageId = messageId;
		this.skillNameId = DataManager.SKILL_DATA.getSkillTemplate(skillListEntry.getSkillId()).getNameId();
		this.skillLvl = String.valueOf(skillListEntry.getSkillLevel());
		this.lvlMsg = lvlMsg; // used for stigma
		this.isAutolearn = isAutolearn;
		this.isNew = isNew;
	}

	@Override
	protected void writeImpl(AionConnection con) {

		final int size = skillList.size();
		writeH(size); // skills list size
		writeC(isAutolearn ? 1 : 0); // 1 all learned skills, 0 skills can be learned ?

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
				int skillType = entry.getSkillType();
				if (CustomConfig.REQ_STIGMA_STONE && entry.isStigma())
					skillType = 1;
				writeC(skillType); // 0 == normal skill , 1 == stigma skill , 3 == linked stigma skill (7 slot when player equip all chargeable stigma
			}
		}
		writeD(messageId);
		if (messageId != 0) {
			writeH(0x24); // unk
			writeD(skillNameId);
			writeH(0x00);
			writeS(lvlMsg != null ? lvlMsg : skillLvl);
			writeH(0x00); // 4.8 - could be S as well if empty
		}
	}
}
