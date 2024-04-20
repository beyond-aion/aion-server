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

	private int skillId;
	private int action;
	private int itemId;
	private int success;
	private int failure;
	private String l10n;
	private int executionSpeed;
	private int delay;

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
			case 0: // init
				writeSystemMsgInfo(STR_EXTRACT_GATHER_START_1_BASIC(null).getId());
				break;
			case 1: // For updates both for ground and aerial
			case 2: // Light blue bar = +10%
			case 3: // Purple bar = 100%
				writeSystemMsgInfo(0);
				break;
			case 5: // canceled
				writeSystemMsgInfo(STR_EXTRACT_GATHER_CANCEL_1_BASIC().getId());
				break;
			case 6: // success
				writeSystemMsgInfo(STR_EXTRACT_GATHER_SUCCESS_1_BASIC(null).getId());
				break;
			case 7: // failure
				writeSystemMsgInfo(STR_EXTRACT_GATHER_FAIL_1_BASIC(null).getId());
				break;
			case 8: // occupied by another player
				writeSystemMsgInfo(STR_EXTRACT_GATHER_OCCUPIED_BY_OTHER().getId());
				break;
		}
	}

	private void writeSystemMsgInfo(int msgId) {
		writeD(msgId); // msgId
		writeS(l10n); // parameter
	}
}
