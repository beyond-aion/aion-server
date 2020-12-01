package ai.instance.drakenspire;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("wave_event_bomber")
public class WaveEventBomberAI extends GeneralNpcAI {

	public WaveEventBomberAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		PacketSendUtility.broadcastMessage(getOwner(), 1501312, 4000);
	}
}
