package com.aionemu.gameserver.network.aion.serverpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.*;

import com.aionemu.gameserver.model.templates.gather.GatherableTemplate;
import com.aionemu.gameserver.model.templates.gather.Material;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * This packet updates the players current gathering status / progress.
 * 
 * @author ATracer, orz, Yeats, Neon
 */
public class SM_GATHER_UPDATE extends AionServerPacket {

	private final int skillId;
	private final int action;
	private final int itemId;
	private final int success;
	private final int failure;
	private final String l10n;
	private final int executionSpeed;
	private final int delay;

	public SM_GATHER_UPDATE(GatherableTemplate template, Material material, int success, int failure, int action, int executionSpeed, int delay) {
		this.skillId = template.getHarvestSkill();
		this.action = action;
		this.itemId = material.getItemId();
		this.success = success;
		this.failure = failure;
		this.executionSpeed = executionSpeed;
		this.delay = delay;
		this.l10n = material.getL10n();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillId);
		writeC(action);
		writeD(itemId);
		writeD(success);
		writeD(failure);
		writeD(executionSpeed);
		writeD(delay);
		switch (action) {
			case 0 -> writeSystemMsgInfo(STR_EXTRACT_GATHER_START_1_BASIC(null).getId()); // init
			case 1 -> writeSystemMsgInfo(0); // For updates both for ground and aerial
			case 2 -> writeSystemMsgInfo(0); // Light blue bar = +10%
			case 3 -> writeSystemMsgInfo(0); // Purple bar = 100%
			case 5 -> writeSystemMsgInfo(STR_EXTRACT_GATHER_CANCEL_1_BASIC().getId()); // canceled
			case 6 -> writeSystemMsgInfo(STR_EXTRACT_GATHER_SUCCESS_1_BASIC(null).getId()); // success
			case 7 -> writeSystemMsgInfo(STR_EXTRACT_GATHER_FAIL_1_BASIC(null).getId()); // failure
			case 8 -> writeSystemMsgInfo(STR_EXTRACT_GATHER_OCCUPIED_BY_OTHER().getId()); // deselects target
		}
	}

	private void writeSystemMsgInfo(int msgId) {
		writeD(msgId); // msgId
		writeS(msgId == 0 ? null : l10n); // parameter
	}
}
