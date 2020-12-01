package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("tiamateye")
public class TiamatEyeAI extends NpcAI {

	public TiamatEyeAI(Npc owner) {
		super(owner);
	}

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
				if (!isDead()) {
					AIActions.deleteOwner(TiamatEyeAI.this);
				}
			}
		}, 5000);
	}
}
