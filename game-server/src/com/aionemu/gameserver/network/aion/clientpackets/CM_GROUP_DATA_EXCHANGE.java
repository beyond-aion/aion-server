package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Collection;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_DATA_EXCHANGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
public class CM_GROUP_DATA_EXCHANGE extends AionClientPacket {

	private int groupType;
	private int action;
	private int unk2;
	private int dataSize;
	private byte[] data;

	public CM_GROUP_DATA_EXCHANGE(int opcode, AionConnection.State state, AionConnection.State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		action = readUC();

		switch (action) {
			case 1:
				dataSize = readD();
				break;
			default:
				groupType = readUC();
				unk2 = readUC();
				dataSize = readD();
				break;
		}
		if (dataSize > 0 && dataSize <= 5086)
			data = readB(dataSize);
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null || data == null) {
			return;
		}

		if (action == 1) {
			PacketSendUtility.broadcastPacketAndReceive(player, new SM_GROUP_DATA_EXCHANGE(data));
			return;
		}

		Collection<Player> players = null;
		switch (groupType) {
			case 0:
				if (player.isInGroup()) {
					players = player.getPlayerGroup().getOnlineMembers();
				}
				break;
			case 1:
				if (player.isInAlliance()) {
					players = player.getPlayerAllianceGroup().getOnlineMembers();
				}
				break;
			case 2:
				if (player.isInLeague()) {
					players = player.getPlayerAllianceGroup().getOnlineMembers();
				}
				break;
		}

		if (players != null) {
			for (Player member : players) {
				if (!member.equals(player)) {
					PacketSendUtility.sendPacket(member, new SM_GROUP_DATA_EXCHANGE(data, action, unk2));
				}
			}
		}
	}

}
