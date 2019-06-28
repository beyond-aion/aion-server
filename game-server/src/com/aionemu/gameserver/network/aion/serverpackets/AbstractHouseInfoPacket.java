package com.aionemu.gameserver.network.aion.serverpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.AbstractPlayerInfoPacket.CHARNAME_MAX_LENGTH;

import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.team.legion.LegionEmblem;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.templates.housing.BuildingType;
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

		writePartData(house, PartType.ROOF, 0, true);
		writePartData(house, PartType.OUTWALL, 0, true);
		writePartData(house, PartType.FRAME, 0, true);
		writePartData(house, PartType.DOOR, 0, true);
		writePartData(house, PartType.GARDEN, 0, true);
		writePartData(house, PartType.FENCE, 0, true);

		for (int room = 0; room < 6; room++) {
			writePartData(house, PartType.INWALL_ANY, room, room > 0);
		}

		for (int room = 0; room < 6; room++) {
			writePartData(house, PartType.INFLOOR_ANY, room, room > 0);
		}

		writePartData(house, PartType.ADDON, 0, true);
		writeD(0);
		writeD(0);
		writeC(0);

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

	private void writePartData(House house, PartType partType, int room, boolean skipPersonal) {
		if (skipPersonal && house.getBuilding().getType() == BuildingType.PERSONAL_INS)
			writeD(0);
		else {
			HouseDecoration deco = house.getRegistry().getRenderPart(partType, room);
			writeD(deco != null ? deco.getTemplate().getId() : 0);
		}
	}
}
