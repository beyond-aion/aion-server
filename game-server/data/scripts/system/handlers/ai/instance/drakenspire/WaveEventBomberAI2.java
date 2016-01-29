package ai.instance.drakenspire;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;


/**
 * @author Estrayl
 */
@AIName("wave_event_bomber")
public class WaveEventBomberAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleMessage();
	}
	
	private void scheduleMessage() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(true, 1501312, getOwner().getObjectId(), 1));
			}
		}, 4000);
	}
}
