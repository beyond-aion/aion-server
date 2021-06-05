package com.aionemu.gameserver.network.aion.clientpackets;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.NetworkUtils;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_DATA_EXCHANGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
public class CM_GROUP_DATA_EXCHANGE extends AionClientPacket {

	/**
	 * Maximum size of the exchange data. The size is determined by subtracting the maximum usable packet body size in bytes by the overhead bytes
	 * required to send it via SM_GROUP_DATA_EXCHANGE
	 */
	private static final int MAX_EXCHANGE_DATA_SIZE = AionServerPacket.MAX_USABLE_PACKET_BODY_SIZE - 6;

	private int groupType;
	private int action;
	private int unk2;
	private byte[] data;

	public CM_GROUP_DATA_EXCHANGE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		action = readUC();
		if (action != 1) {
			groupType = readUC();
			unk2 = readUC();
		}
		int dataSize = readD();
		data = readB(dataSize);
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null || data.length == 0)
			return;

		if (data.length > MAX_EXCHANGE_DATA_SIZE) {
			LoggerFactory.getLogger(CM_GROUP_DATA_EXCHANGE.class).error(
				"Player {} exceeded maximum exchange data size (action: {}, groupType: {}, unk2: {}, bytes send: {}): \n{}", player, action, groupType, unk2,
				data.length, NetworkUtils.toHex(ByteBuffer.wrap(data)));
			return;
		}

		if (action == 1) {
			PacketSendUtility.broadcastPacketAndReceive(player, new SM_GROUP_DATA_EXCHANGE(data));
			return;
		}
		List<Player> players = null;
		switch (groupType) {
			case 0:
				if (player.isInGroup())
					players = player.getPlayerGroup().getOnlineMembers();
				break;
			case 1:
				if (player.isInAlliance())
					players = player.getPlayerAllianceGroup().getOnlineMembers();
				break;
			case 2:
				if (player.isInLeague())
					players = player.getPlayerAllianceGroup().getOnlineMembers();
				break;
		}

		if (players == null || players.isEmpty())
			return;
		SM_GROUP_DATA_EXCHANGE packet = new SM_GROUP_DATA_EXCHANGE(data, action, unk2);
		for (Player member : players) {
			if (!member.equals(player))
				PacketSendUtility.sendPacket(member, packet);
		}
	}

}
