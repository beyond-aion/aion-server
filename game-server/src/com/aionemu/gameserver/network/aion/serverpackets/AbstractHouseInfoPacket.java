package com.aionemu.gameserver.network.aion.serverpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.AbstractPlayerInfoPacket.CHARNAME_MAX_LENGTH;

import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.team.legion.LegionEmblem;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.templates.housing.PartType;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.LegionService;

/**
 * @author Neon
 */
public abstract class AbstractHouseInfoPacket extends AionServerPacket {

	public static final int SIGN_NOTICE_MAX_LENGTH = 64;
	protected final House house;

	protected AbstractHouseInfoPacket(House house) {
		this.house = house;
	}

	protected void writeCommonInfo() {
		LegionMember member = house.isInactive() || house.getOwnerId() == 0 ? null : LegionService.getInstance().getLegionMember(house.getOwnerId());

		writeD(0);
		writeD(house.getAddress().getId());
		writeD(house.getOwnerId());
		writeD(house.getBuilding().getType().getId());
		writeC(1); // unk

		writeD(house.getBuilding().getId());
		writeC(house.getHouseOwnerStates());
		writeC(house.getDoorState().getId());

		writeS(house.getOwnerName(), CHARNAME_MAX_LENGTH);

		writeD(member == null ? 0 : member.getLegion().getLegionId());

		writeC(house.isShowOwnerName() ? 1 : 0);
		writeS(house.getSignNotice(), SIGN_NOTICE_MAX_LENGTH); // client can display much longer strings but then decor won't show

		for (PartType partType : PartType.values()) {
			for (int roomNo = 0; roomNo < partType.getRooms(); roomNo++) {
				Integer decorId = house.getRegistry().getUsedDecorId(partType, roomNo);
				writeD(decorId == null ? 0 : decorId);
			}
		}
		writeD(0);
		writeD(0);
		writeC(0); // show legion flags near house door: 0 = none, 1 = left, 2 = right (1+2 = both)
		// Emblem and color
		if (member == null || member.getLegion().getLegionEmblem() == null) {
			writeB(new byte[6]);
		} else {
			LegionEmblem emblem = member.getLegion().getLegionEmblem();
			writeC(emblem.getEmblemId());
			writeC(emblem.getEmblemType().getValue());
			writeC(emblem.getColor_a());
			writeC(emblem.getColor_r());
			writeC(emblem.getColor_g());
			writeC(emblem.getColor_b());
		}
	}
}
