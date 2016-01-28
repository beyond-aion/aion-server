package com.aionemu.gameserver.network.aion.iteminfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.templates.item.Stigma;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * This blob contains stigma info.
 * 
 * @author -Nemesiss-
 * @modified Rolandas, Neon
 */
public class StigmaInfoBlobEntry extends ItemBlobEntry {

	StigmaInfoBlobEntry() {
		super(ItemBlobType.STIGMA_INFO);
	}

	@Override
	public void writeThisBlob(ByteBuffer buf) {
		Stigma stigma = ownerItem.getItemTemplate().getStigma();
		List<SkillTemplate> group1 = stigma.getGroupSkillTemplates(1);
		List<SkillTemplate> group2 = stigma.getGroupSkillTemplates(2);

		writeD(buf, group1.get(0).getSkillId()); // skill id 1
		writeD(buf, group2.size() > 0 ? group2.get(0).getSkillId() : 0); // skill id 2
		writeD(buf, 0); // Shard count in 4.7

		skip(buf, 192);
		writeH(buf, 0x1); // unk
		writeH(buf, 0);
		skip(buf, 96);
		writeH(buf, 0); // unk
	}

	@Override
	public int getSize() {
		return 12 + 192 + 4 + 96 + 2;
	}
}
