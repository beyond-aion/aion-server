package com.aionemu.gameserver.services.panesterra.ahserion;

import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SIEGE_LOCATION_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Yeats
 * @modified Estrayl
 */
public class AhserionTeam extends PanesterraTeam {
	
	private AtomicInteger usedChariots = new AtomicInteger();
	private AtomicInteger usedIgnusEngines = new AtomicInteger();

	public AhserionTeam(PanesterraTeamId id) {
		super(id);
	}
	
	public int getUsedChariots() {
		return usedChariots.get();
	}
	
	public int getUsedIgnusEngines() {
		return usedIgnusEngines.get();
	}
	
	public boolean canUseChariot() {
		return !isEliminated.get() || usedChariots.incrementAndGet() <= 8;
	}
	
	public boolean canUseIgnusEngine() {
		return !isEliminated.get() || usedIgnusEngines.incrementAndGet() <= 8;
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
	
	@Override
	protected void sendPackets(Player player) {
		PacketSendUtility.sendPacket(player, new SM_SIEGE_LOCATION_INFO());
	}
}