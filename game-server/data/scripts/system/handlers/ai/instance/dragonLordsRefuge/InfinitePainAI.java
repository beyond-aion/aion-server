package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("infinitepain")
public class InfinitePainAI extends NpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		gravitationalDisturbance();
	}

	private void gravitationalDisturbance() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.useSkill(InfinitePainAI.this, 20969);
				despawn();
			}
		}, 2000);
	}

	private void despawn() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				getOwner().getController().delete();
			}
		}, 5000);
	}
}
