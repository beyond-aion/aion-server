package ai.instance.tiamatStrongHold;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.services.NpcShoutsService;

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
				NpcShoutsService.getInstance().sendMsg(getOwner(), 1500679, getOwner().getObjectId(), 0, 2000);
				break;
			case 283914:
				NpcShoutsService.getInstance().sendMsg(getOwner(), 1500680, getOwner().getObjectId(), 0, 2000);
				break;
			case 283915:
				NpcShoutsService.getInstance().sendMsg(getOwner(), 1500681, getOwner().getObjectId(), 0, 2000);
				break;
			case 283916:
				NpcShoutsService.getInstance().sendMsg(getOwner(), 1500682, getOwner().getObjectId(), 0, 2000);
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
