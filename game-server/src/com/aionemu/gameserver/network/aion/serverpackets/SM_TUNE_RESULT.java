package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.PendingTuneResult;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * //fsc 0x120 ddhcdhdddddddbdcbcc 107015 1 2 10 110551054 1281 0 0 0 0 0 0 0 13 0 2 120 1 1
 * 
 * @author Estrayl
 */
public class SM_TUNE_RESULT extends AionServerPacket {

	private final Item targetItem;
	private final int tuningScrollObjectId;
	private final int rndBonus;
	private int enchantBonus;
	private int optionalSockets;

	public SM_TUNE_RESULT(Item targetItem, int tuningScrollObjectId, PendingTuneResult result) {
		this.targetItem = targetItem;
		this.tuningScrollObjectId = tuningScrollObjectId;
		rndBonus = result.getBonusSetId();
		if (result.shouldNotReduceTuneCount()) {
			enchantBonus = targetItem.getEnchantBonus();
			optionalSockets = targetItem.getOptionalSockets();
		} else {
			enchantBonus = result.getEnchantBonus();
			optionalSockets = result.getOptionalSockets();
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetItem.getObjectId());
		writeD(tuningScrollObjectId);
		writeH(rndBonus);
		writeC(targetItem.getEnchantLevel());
		writeD(targetItem.getItemId());
		writeH(enchantBonus * 256 + optionalSockets);
		if (targetItem.hasManaStones()) {
			Set<ManaStone> itemStones = targetItem.getItemStones();
			Map<Integer, ManaStone> stonesBySlot = new HashMap<>();
			for (ManaStone itemStone : itemStones) {
				stonesBySlot.put(itemStone.getSlot(), itemStone);
			}
			for (int i = 0; i < Item.MAX_BASIC_STONES; i++) {
				ManaStone stone = stonesBySlot.get(i);
				if (stone == null)
					writeD(0);
				else
					writeD(stone.getItemId());
			}
		} else {
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}
		writeD(targetItem.getGodStoneId());
		writeB(new byte[13]);
		writeD(targetItem.getIdianStone() != null ? targetItem.getIdianStone().getItemId() : 0);
		writeC(2); // UNK 0 or 2
		writeB(new byte[120]);
		writeC(0); // UNK
		writeC(0); // UNK
	}
}
