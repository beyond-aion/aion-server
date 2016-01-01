package ai.instance.elementisForest;

import java.util.concurrent.Future;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;

/**
 * @author Luzien
 */
@AIName("canyonfragment")
public class CanyonFragmentAI2 extends AggressiveNpcAI2 {

	private Future<?> task;

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		schedule();
	}

	@Override
	public void handleDied() {
		super.handleDied();
		if (!task.isDone())
			task.cancel(false);
	}

	private void schedule() {
		task = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					spawn(282430, getPosition().getX(), getPosition().getY(), getPosition().getZ(), (byte) 0);
					AI2Actions.deleteOwner(CanyonFragmentAI2.this);
				}
			}

		}, 25000);
	}

}
