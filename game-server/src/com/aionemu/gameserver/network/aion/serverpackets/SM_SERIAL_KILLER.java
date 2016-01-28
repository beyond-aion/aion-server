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
	private int cooldown;
	private Player player;
	private Collection<Player> players;

	public SM_SERIAL_KILLER(int type, int debuffLvl, int cooldown) {
		this.type = type;
		this.debuffLvl = debuffLvl;
		this.cooldown = cooldown;
	}

	public SM_SERIAL_KILLER(int type, int debuffLvl) {
		this.type = type;
		this.debuffLvl = debuffLvl;
	}
	
	public SM_SERIAL_KILLER(int type, Player player) {
		this.type = type;
		this.player = player;
	}

	public SM_SERIAL_KILLER(Collection<Player> players) {
		this.type = 5;
		this.players = players;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(type);
		writeD(0x01);
		writeD(0x01);
		switch (type) {
			case 0: //killer + no announcement
			case 1: //killer + announcement
			case 7: //guard + cd no announcement
			case 8: //guard + announcement
				writeH(0x01);
				writeD(debuffLvl);
				writeD(cooldown); //cooldown
				break;
			case 5:
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
					writeD(1941); // unk
					writeD(1942); // unk
					writeD(1943); // unk
					writeD(1944); // unk
					writeH(7); // unk
				}
				break;
			case 6: //killer
			case 9: //guard
				writeH(0x01);// unk
				writeD(player.getSKInfo().getRank());
				writeD(player.getObjectId());
				break;
		}
	}
}
