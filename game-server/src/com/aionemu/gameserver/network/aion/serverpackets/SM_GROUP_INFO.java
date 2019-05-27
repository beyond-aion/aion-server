package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamType;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Lyahim, ATracer, xTz
 */
public class SM_GROUP_INFO extends AionServerPacket {

	private final LootGroupRules lootRules;
	private final int groupId;
	private final int leaderId;
	private final TeamType type;

	public SM_GROUP_INFO(PlayerGroup group) {
		groupId = group.getObjectId();
		leaderId = group.getLeader().getObjectId();
		lootRules = group.getLootGroupRules();
		type = group.getTeamType();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player player = con.getActivePlayer();
		writeD(groupId);
		writeD(leaderId);
		writeD(player == null || player.getPosition() == null ? 0 : player.getWorldId());// mapId
		writeD(lootRules.getLootRule().getId());
		writeD(lootRules.getMisc());
		writeD(lootRules.getCommonItemAbove());
		writeD(lootRules.getSuperiorItemAbove());
		writeD(lootRules.getHeroicItemAbove());
		writeD(lootRules.getFabledItemAbove());
		writeD(lootRules.getEternalItemAbove());
		writeD(lootRules.getMythicItemAbove());
		writeD(0x02);
		writeC(0x00);
		writeD(type.getType());
		writeD(type.getSubType());
		writeD(0x00); // message id
		writeS(""); // name
	}

}
