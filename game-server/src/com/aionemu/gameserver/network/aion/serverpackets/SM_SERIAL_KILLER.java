package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Source & xTz
 */
public class SM_SERIAL_KILLER extends AionServerPacket {

	private int type;
	private int debuffLvl;
	private Collection<Player> players;

	public SM_SERIAL_KILLER(boolean showMsg, int debuffLvl) {
		this.type = showMsg ? 1 : 0;
		this.debuffLvl = debuffLvl;
	}

	public SM_SERIAL_KILLER(Collection<Player> players) {
		this.type = 4;
		this.players = players;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		switch (type) {
			case 0:
			case 1:
				writeD(type);
				writeD(0x01);
				writeD(0x01);
				writeH(0x01);
				writeD(debuffLvl);
				break;
			case 4:
				writeD(type);
				writeD(0x01); // unk
				writeD(0x01); // unk
				writeH(players.size());
				for (Player player : players) {
					writeD(player.getSKInfo().getRank());
					writeD(player.getObjectId());
					writeD(0x01); // unk
					writeD(player.getAbyssRank().getRank().getId());
					writeH(player.getLevel());
					writeF(player.getX());
					writeF(player.getY());
					writeS(player.getName(), 118);
					writeD(0x00); // unk
					writeD(0x00); // unk
					writeD(0x00); // unk
					writeD(0x00); // unk
				}
				break;
		}
	}

}
