package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_SHOW_BRAND extends AionServerPacket {

	private final Map<Integer, Integer> targetIdsByIconId = new HashMap<>();

	public SM_SHOW_BRAND(int iconId, int targetObjectId) {
		targetIdsByIconId.put(iconId, targetObjectId);
	}

	public SM_SHOW_BRAND(Map<Integer, Integer> targetIdsByIconId) {
		if (targetIdsByIconId.isEmpty()) {
			IntStream.range(0, 16).forEach(brandId -> this.targetIdsByIconId.put(brandId, 0)); // reset all brands
		} else {
			this.targetIdsByIconId.putAll(targetIdsByIconId);
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(targetIdsByIconId.size());
		targetIdsByIconId.forEach((iconId, targetObjectId) -> {
			writeD(1); // 0 = solo?, 1 = group/alliance?, 2 = league? - doesn't seem to make any difference
			writeD(iconId);
			writeD(targetObjectId); // 0 = remove icon
		});
	}
}
