package com.aionemu.gameserver.network.aion.skillinfo;

import java.nio.ByteBuffer;
import java.util.function.Function;

import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.PacketWriteHelper;

public class SkillEntryWriter extends PacketWriteHelper {

	public static final Function<PlayerSkillEntry, Integer> DYNAMIC_BODY_PART_SIZE_CALCULATOR = (skill) -> 11;

	private final PlayerSkillEntry skillEntry;

	public static void writeSkillEntry(PlayerSkillEntry skillEntry, ByteBuffer buffer) {
		SkillEntryWriter entryWriter = new SkillEntryWriter(skillEntry);
		entryWriter.writeMe(buffer);
	}

	public SkillEntryWriter(PlayerSkillEntry skillEntry) {
		this.skillEntry = skillEntry;
	}

	@Override
	protected void writeMe(ByteBuffer buf) {
		writeH(buf, skillEntry.getSkillId());
		writeH(buf, skillEntry.isNormalSkill() ? 1 : skillEntry.getSkillLevel());
		writeC(buf, 0x00);
		writeC(buf, skillEntry.getProfessionSkillBarSize());
		writeD(buf, skillEntry.isProfessionSkill() ? skillEntry.getProfessionFlag() : skillEntry.getFlag());
		writeC(buf, skillEntry.getSkillType()); // 0 normal skill , 1 stigma skill , 3 linked stigma skill
	}
}
