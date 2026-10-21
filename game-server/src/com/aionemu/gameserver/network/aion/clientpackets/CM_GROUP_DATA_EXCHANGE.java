package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.ClientBroadcastType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_DATA_EXCHANGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author xTz
 */
public class CM_GROUP_DATA_EXCHANGE extends AionClientPacket {

	private static final int MAX_EXCHANGE_DATA_SIZE = 6144;

	private ClientBroadcastType type;
	private Team team;
	private int tag;
	private int dataSize;
	private byte[] data;

	public CM_GROUP_DATA_EXCHANGE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		type = ClientBroadcastType.getById(readUC());
		if (type == ClientBroadcastType.TEAM) {
			team = Team.getById(readUC());
			tag = readUC();
		}
		dataSize = readD();
		if (dataSize >= 0 && dataSize <= MAX_EXCHANGE_DATA_SIZE && dataSize <= getRemainingBytes())
			data = readB(dataSize);
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;
		if (data == null) {
			AuditLogger.log(player, "sent client broadcast data of " + dataSize + " bytes (type " + type + ")");
			return;
		}
		if (data.length == 0 || type == null)
			return;

		if (type != ClientBroadcastType.TEAM) {
			broadcastOnHouseLand(player);
			return;
		}
		List<Player> players = team == null ? null : switch (team) {
			case GROUP -> player.isInGroup() ? player.getPlayerGroup().getOnlineMembers() : null;
			case ALLIANCE -> player.isInAlliance() ? player.getPlayerAlliance().getOnlineMembers() : null;
			case LEAGUE -> player.isInLeague() ? player.getPlayerAlliance().getLeague().getOnlineMembers() : null;
		};
		if (players == null || players.isEmpty())
			return;
		SM_GROUP_DATA_EXCHANGE packet = new SM_GROUP_DATA_EXCHANGE(tag, data);
		for (Player member : players) {
			if (!member.equals(player))
				PacketSendUtility.sendPacket(member, packet);
		}
	}

	/**
	 * House scripts talk to everyone on the house land the sender stands on. Lands have no separate area for the inside of the house, so the
	 * house-only types reach the whole land like {@link ClientBroadcastType#HOUSE_LAND}.
	 */
	private void broadcastOnHouseLand(Player player) {
		player.getPosition().getMapRegion().findZones(player).stream().map(zone -> zone.getZoneTemplate().getName())
			.filter(name -> name.startsWith("HOUSE_")).findFirst()
			.ifPresent(land -> PacketSendUtility.broadcastPacket(player, new SM_GROUP_DATA_EXCHANGE(data), true, other -> other.isInsideZone(land)));
	}

	private enum Team {

		GROUP(0),
		ALLIANCE(1),
		LEAGUE(2);

		private final int id;

		Team(int id) {
			this.id = id;
		}

		static Team getById(int id) {
			for (Team team : values())
				if (team.id == id)
					return team;
			return null;
		}
	}
}
