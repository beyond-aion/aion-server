package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.skillinfo.SkillEntryWriter;

/**
 * @author Yeats
 */
public class SM_GM_SHOW_PLAYER_SKILLS extends AionServerPacket {

	public static final int STATIC_BODY_SIZE = 2;

	private final List<PlayerSkillEntry> skillList;

	public SM_GM_SHOW_PLAYER_SKILLS(List<PlayerSkillEntry> skillList) {
		this.skillList = skillList;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillList.size()); // skills list size
		for (PlayerSkillEntry entry : skillList)
			SkillEntryWriter.writeSkillEntry(entry, getBuf());
	}
}
