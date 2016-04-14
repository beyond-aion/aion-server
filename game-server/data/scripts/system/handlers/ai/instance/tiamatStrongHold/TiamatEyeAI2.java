package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("tiamateye")
public class TiamatEyeAI2 extends NpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		int owner = getOwner().getNpcId();
		switch (owner) {
			case 283913:
				PacketSendUtility.broadcastMessage(getOwner(), 1500679, 2000);
				break;
			case 283914:
				PacketSendUtility.broadcastMessage(getOwner(), 1500680, 2000);
				break;
			case 283915:
				PacketSendUtility.broadcastMessage(getOwner(), 1500681, 2000);
				break;
			case 283916:
				PacketSendUtility.broadcastMessage(getOwner(), 1500682, 2000);
				break;
		}
		despawn();
	}

	private void despawn() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					AI2Actions.deleteOwner(TiamatEyeAI2.this);
				}
			}
		}, 5000);
	}
}
