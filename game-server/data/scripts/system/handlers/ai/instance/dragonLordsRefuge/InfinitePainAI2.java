package ai.instance.dragonLordsRefuge;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;

/**
 * @author Cheatkiller
 */
@AIName("infinitepain")
public class InfinitePainAI2 extends NpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		gravitationalDisturbance();
	}

	private void gravitationalDisturbance() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.useSkill(InfinitePainAI2.this, 20969);
				despawn();
			}
		}, 2000);
	}

	private void despawn() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				getOwner().getController().onDelete();
			}
		}, 5000);
	}
}
