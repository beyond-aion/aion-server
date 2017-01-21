package ai.instance.drakenspire;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI;


/**
 * @author Estrayl
 */
@AIName("wave_event_bomber")
public class WaveEventBomberAI extends GeneralNpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		PacketSendUtility.broadcastMessage(getOwner(), 1501312, 4000);
	}
}
