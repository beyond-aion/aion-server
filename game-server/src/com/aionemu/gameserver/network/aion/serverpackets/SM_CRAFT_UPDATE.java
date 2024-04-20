package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Mr. Poke, Yeats
 */
public class SM_CRAFT_UPDATE extends AionServerPacket {

	private int skillId;
	private int itemId;
	private int action;
	private int success;
	private int failure;
	private String itemNameL10n;
	private int executionSpeed;
	private int delay;

	public SM_CRAFT_UPDATE(int skillId, ItemTemplate item, int success, int failure, int action, int executionSpeed, int delay) {
		this.action = action;
		this.skillId = skillId;
		this.itemId = item.getTemplateId();
		this.success = success;
		this.failure = failure;
		this.itemNameL10n = item.getL10n();
		this.executionSpeed = executionSpeed;
		if (skillId == 40009) {
			this.delay = 1000;
		} else {
			this.delay = delay;
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(skillId);
		writeC(action);
		writeD(itemId);
		writeD(success); // max
		writeD(failure); // max
		writeD(executionSpeed);
		writeD(delay); // delay

		switch (action) {
			case 0: // init
			case 3: // crit = proc
				writeD(1330048); // msgId
				writeS(itemNameL10n); // param
				break;
			case 1: // update (normal)
			case 2: // crit (blue) = +10%
				writeD(0);
				writeS(null);
				break;
			case 4: // cancelled
				writeD(1330051);
				writeS(null);
				break;
			case 5: // success (end)
				writeD(1330049);
				writeS(itemNameL10n); // param
				break;
			case 6: // failed (end)
			case 7: // failure (never used?)
				writeD(1330050);
				writeS(itemNameL10n); // param
				break;
		}
	}
}
