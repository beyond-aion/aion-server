package com.aionemu.gameserver.network.aion.serverpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.AbstractPlayerInfoPacket.CHARNAME_MAX_LENGTH;

import java.util.Collection;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.CPInfo;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;

/**
 * SM_SERIAL_KILLER pre 4.8
 * 
 * @author Source, xTz
 */
public class SM_CONQUEROR_PROTECTOR extends AionServerPacket {

	private int type;
	private int buffLvl;
	private int cooldown;
	private Player player;
	private Collection<Player> intruders;

	public SM_CONQUEROR_PROTECTOR(int type, int buffLvl, int cooldown) {
		this.type = type;
		this.buffLvl = buffLvl;
		this.cooldown = cooldown;
	}

	public SM_CONQUEROR_PROTECTOR(int type, int buffLvl) {
		this.type = type;
		this.buffLvl = buffLvl;
	}

	public SM_CONQUEROR_PROTECTOR(int type, Player player) {
		this.type = type;
		this.player = player;
	}

	public SM_CONQUEROR_PROTECTOR(Collection<Player> intruders, boolean displayCd) {
		this.type = displayCd ? 5 : 4;
		this.intruders = intruders;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(type);
		writeD(0x01);
		writeD(0x01);
		switch (type) {
			case 0: // conqueror + no announcement
			case 1: // conqueror + announcement
			case 7: // protector + cd no announcement
			case 8: // protector + announcement
				writeH(0x01);
				writeD(buffLvl);
				writeD(cooldown); // intruder scan cooldown
				break;
			case 4: // intruder scan (without cd)
			case 5: // intruder scan (with cd)
				writeH(intruders.size());
				for (Player player : intruders) {
					CPInfo info = ConquerorAndProtectorService.getInstance().getCPInfoForCurrentMap(player);
					writeD(info == null ? 0 : info.getRank());
					writeD(player.getObjectId());
					writeD(0x01); // unk
					writeD(player.getAbyssRank().getRank().getId());
					writeH(player.getLevel());
					writeF(player.getX());
					writeF(player.getY());
					writeS(player.getName(true), CHARNAME_MAX_LENGTH);
					writeB(new byte[66]); // unk
					writeD(1941); // unk
					writeD(1942); // unk
					writeD(1943); // unk
					writeD(1944); // unk
					writeH(7); // unk
				}
				break;
			case 6: // conqueror
			case 9: // protector
				writeH(0x01);// unk
				CPInfo info = ConquerorAndProtectorService.getInstance().getCPInfoForCurrentMap(player);
				writeD(info == null ? 0 : info.getRank());
				writeD(player.getObjectId());
				break;
		}
	}
}
