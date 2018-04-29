package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.PendingTuneResult;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.iteminfo.EnchantInfoBlobEntry;

/**
 * @author Estrayl
 */
public class SM_TUNE_RESULT extends AionServerPacket {

	private final Item targetItem;
	private final int tuningScrollObjectId;
	private final PendingTuneResult result;

	public SM_TUNE_RESULT(Item targetItem, int tuningScrollObjectId, PendingTuneResult result) {
		this.targetItem = targetItem;
		this.tuningScrollObjectId = tuningScrollObjectId;
		this.result = result;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetItem.getObjectId());
		writeD(tuningScrollObjectId);
		writeC(result.getStatBonusId());
		EnchantInfoBlobEntry.writeInfo(getBuf(), targetItem, result.getOptionalSockets(), result.getEnchantBonus());
		writeB(new byte[36]);
		writeC(0); // UNK
		writeC(0); // UNK
	}
}
