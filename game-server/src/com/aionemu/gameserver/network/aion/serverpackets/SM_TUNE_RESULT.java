package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.ItemStone;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.PendingTuneResult;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

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
		writeH(result.getStatBonusId());
		writeC(targetItem.getEnchantLevel());
		writeD(targetItem.getItemId());
		writeC(result.getOptionalSockets());
		writeC(result.getEnchantBonus());
		Map<Integer, ManaStone> stonesBySlot = createManastoneMap(targetItem);
		for (int i = 0; i < Item.MAX_BASIC_STONES; i++) {
			ManaStone stone = stonesBySlot.get(i);
			writeD(stone == null ? 0 : stone.getItemId());
		}
		writeD(targetItem.getGodStoneId());
		writeB(new byte[13]);
		writeD(targetItem.getIdianStone() != null ? targetItem.getIdianStone().getItemId() : 0);
		writeC(2); // UNK 0 or 2
		writeB(new byte[120]);
		writeC(0); // UNK
		writeC(0); // UNK
	}

	private Map<Integer, ManaStone> createManastoneMap(Item item) {
		if (item.hasManaStones())
			return item.getItemStones().stream().collect(Collectors.toMap(ItemStone::getSlot, s -> s));
		else
			return Collections.emptyMap();
	}
}
