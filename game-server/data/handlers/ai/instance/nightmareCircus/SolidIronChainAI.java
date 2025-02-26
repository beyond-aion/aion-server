package ai.instance.nightmareCircus;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("solidironchain")
public class SolidIronChainAI extends AggressiveNpcAI {

	public SolidIronChainAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		PacketSendUtility.broadcastToMap(getOwner(), new SM_PLAY_MOVIE(false, 0, 0, 983, true));
	}

}
