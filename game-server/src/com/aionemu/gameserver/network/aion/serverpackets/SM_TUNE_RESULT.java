package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.PendingTuneResult;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.iteminfo.EnchantInfoBlobEntry;

/**
 * @author Estrayl, Sykra
 */
public class SM_TUNE_RESULT extends AionServerPacket {

	private final Item targetItem;
	private final int tuningScrollItemId;
	private final PendingTuneResult result;
	private final boolean tuneCancelPossible;
	private final boolean showManastoneSlots;

	public SM_TUNE_RESULT(Item targetItem, int tuningScrollItemId, PendingTuneResult result) {
		this.targetItem = targetItem;
		this.tuningScrollItemId = tuningScrollItemId;
		this.result = result;
		this.tuneCancelPossible = this.showManastoneSlots = !result.isAttributeOnly();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetItem.getObjectId());
		writeD(tuningScrollItemId);
		writeC(result.getStatBonusId());
		EnchantInfoBlobEntry.writeInfo(getBuf(), targetItem, result.getOptionalSockets(), result.getEnchantBonus());
		writeC(showManastoneSlots ? 0 : 1);
		writeC(tuneCancelPossible ? 0 : 1);
	}
}
