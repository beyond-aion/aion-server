package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collections;
import java.util.Set;

import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.drop.DropRegistrationService;

/**
 * @author alexa026
 */
public class SM_LOOT_STATUS extends AionServerPacket {

	private final int targetObjectId;
	private final Status status;
	private final int lootEffectId;

	public SM_LOOT_STATUS(int targetObjectId, Status status) {
		this.targetObjectId = targetObjectId;
		this.status = status;
		this.lootEffectId = status == Status.LOOT_ENABLE ? getLootEffect(targetObjectId) : 0;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjectId);
		writeC(status.getId());
		writeD(lootEffectId);
	}

	private static int getLootEffect(int targetObjectId) {
		Set<DropItem> items = DropRegistrationService.getInstance().getCurrentDropMap().getOrDefault(targetObjectId, Collections.emptySet());
		return items.stream().mapToInt(DropItem::getLootEffectId).filter(i -> i != 0).findAny().orElse(0);
	}

	public enum Status {
		LOOT_ENABLE(0),
		LOOT_DISABLE(1),
		OPEN_DROP_LIST(2),
		CLOSE_DROP_LIST(3);

		private final int id;

		Status(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}
}
