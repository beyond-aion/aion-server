package ai.instance.tiamatStrongHold;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller, Luzien
 */
@AIName("gravity")
public class GravityAI2 extends NpcAI2 {

	private Future<?> task;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				AI2Actions.useSkill(GravityAI2.this, 20738);
			}
		}, 0, 3250);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.deleteOwner(GravityAI2.this);
			}
		}, 20000);
	}

	@Override
	public void handleDespawned() {
		if (task != null)
			task.cancel(true);
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
