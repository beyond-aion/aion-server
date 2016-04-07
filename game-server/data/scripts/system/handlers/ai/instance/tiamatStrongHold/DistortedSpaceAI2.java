package ai.instance.tiamatStrongHold;

import java.util.concurrent.Future;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIQuestion;

/**
 * @author Cheatkiller
 */
@AIName("distortedspace")
public class DistortedSpaceAI2 extends NpcAI2 {

	private Future<?> task;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		useskill();
	}

	private void useskill() {
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (getOwner().getNpcId() == 283097)
					AI2Actions.useSkill(DistortedSpaceAI2.this, 20740);
			}
		}, 500, 2000);

		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				cancelTask();
				if (getOwner().getNpcId() == 283097)
					AI2Actions.useSkill(DistortedSpaceAI2.this, 20742);
				getOwner().getController().die();
			}
		}, 8000);
	}

	private void cancelTask() {
		if (task != null && !task.isCancelled()) {
			task.cancel(true);
		}
	}

	@Override
	public void handleDied() {
		super.handleDied();
		cancelTask();
		AI2Actions.deleteOwner(this);
	}

	@Override
	public void handleDespawned() {
		super.handleDespawned();
		cancelTask();
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
