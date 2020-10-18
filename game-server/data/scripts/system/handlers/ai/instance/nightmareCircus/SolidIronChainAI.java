package ai.instance.nightmareCircus;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
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

	private AtomicBoolean moviePlayed = new AtomicBoolean();

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		if (moviePlayed.compareAndSet(false, true)) {
			getPosition().getWorldMapInstance().forEachPlayer(p -> PacketSendUtility.sendPacket(p, new SM_PLAY_MOVIE(0, 983)));
		}
	}

}
