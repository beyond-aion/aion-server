package com.aionemu.gameserver.services.panesterra.ahserion;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SIEGE_LOCATION_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Yeats
 *
 */
public class AhserionTeam extends PanesterraTeam {

	private int usedChariots = 0;
	private int usedIgnusEngines = 0;

	public AhserionTeam(PanesterraTeamId id) {
		super(id);
	}
	
	public int getUsedChariots() {
		return this.usedChariots;
	}
	
	public int getUsedIgnusEngines() {
		return this.usedIgnusEngines;
	}
	
	public boolean canUseChariot() {
		return !isEliminated.get() && usedChariots < 8;
	}

	public boolean updateUsedChariots() {
		boolean cap = false;
		synchronized (this) {
			usedChariots++;
			if (usedChariots > 8) {
				cap = true;
			}
		}
		if (cap || isEliminated.get())
			return false;
		return true;
	}
	
	public boolean canUseIgnusEngine() {
		return !isEliminated.get() && usedIgnusEngines < 8;
	}
	
	public boolean updateUsedIgnusEngines() {
		boolean cap = false;
		synchronized (this) {
			usedIgnusEngines++;
			if (usedIgnusEngines > 8) {
				cap = true;
			}
		}
		if (cap || isEliminated.get())
			return false;
		return true;
		
	}

	@Override
	protected void setStartPosition() {
		switch (teamId) {
			case GAB1_SUB_DEST_69:
				startPosition = new WorldPosition(400030000, 285.787f, 287.383f, 681f, (byte) 15);
				break;
			case GAB1_SUB_DEST_70:
				startPosition = new WorldPosition(400030000, 285.787f, 734.389f, 681f, (byte) 105);
				break;
			case GAB1_SUB_DEST_71:
				startPosition = new WorldPosition(400030000, 730.535f, 739.262f, 681f, (byte) 75);
				break;
			case GAB1_SUB_DEST_72:
				startPosition = new WorldPosition(400030000, 734.0557f, 291.067f, 681f, (byte) 45);
				break;
		}
	}

	/**
	 * @return
	 */
	public int getTeamColor() {
		switch (teamId) {
			case GAB1_SUB_DEST_69:
				return -1962786561;
			case GAB1_SUB_DEST_70:
				return 6260223;
			case GAB1_SUB_DEST_71:
				return 1084100351;
			case GAB1_SUB_DEST_72:
				return -1135668993;
				default: 
					return 0;
		}
	}

	/**
	 * @return
	 */
	public String getTeamName() {
		switch (teamId) {
			case GAB1_SUB_DEST_69:
				return "BELUS";
			case GAB1_SUB_DEST_70:
				return "ASPIDA";
			case GAB1_SUB_DEST_71:
				return "ATHANOS";
			case GAB1_SUB_DEST_72:
				return "DEYLON";
				default:
					return "";
		}
	}
	
	@Override
	protected void sendPackets(Player player) {
		PacketSendUtility.sendPacket(player, new SM_SIEGE_LOCATION_INFO());
	}
}