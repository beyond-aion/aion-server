package ai.instance.tallocsHollow;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xTz
 */
@AIName("mosquaegg")
public class MosquaEggAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleSpawned() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				checkSpawn();
			}
		}, 17000);
	}

	private void checkSpawn() {
		if (getPosition().isSpawned()) {
			// spawn - Spawned Supraklaw
			spawn(217132, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
			AI2Actions.deleteOwner(this);
		}
	}
}
