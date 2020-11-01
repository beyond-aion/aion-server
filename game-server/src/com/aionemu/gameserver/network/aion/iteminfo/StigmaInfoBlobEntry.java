package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.templates.item.Stigma;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * This blob contains stigma info.
 * 
 * @author -Nemesiss-, Rolandas, Neon
 */
public class StigmaInfoBlobEntry extends ItemBlobEntry {

	StigmaInfoBlobEntry() {
		super(ItemBlobType.STIGMA_INFO);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		Stigma stigma = ownerItem.getItemTemplate().getStigma();
		List<SkillTemplate> firstGroupSkills = stigma.getGainSkillsByGroup(1);
		List<SkillTemplate> secondGroupSkills = stigma.getGainSkillsByGroup(2);

		writeD(buf, firstGroupSkills == null ? 0 : firstGroupSkills.get(0).getSkillId()); // group 1 skill id
		writeD(buf, secondGroupSkills == null ? 0 : secondGroupSkills.get(0).getSkillId()); // group 2 skill id
		writeD(buf, 0); // Shard count in 4.7

		skip(buf, 192);
		writeH(buf, 0x1); // unk
		writeH(buf, 0);
		skip(buf, 96);
		writeH(buf, 0); // unk
	}

	@Override
	public int getSize() {
		return 306; // 12 + 192 + 4 + 96 + 2
	}
}
