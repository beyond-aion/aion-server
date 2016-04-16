package ai.instance.drakenspire;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI2;


/**
 * @author Estrayl
 */
@AIName("wave_event_bomber")
public class WaveEventBomberAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		PacketSendUtility.broadcastMessage(getOwner(), 1501312, 4000);
	}
}
