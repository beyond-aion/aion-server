package ai.instance.tiamatStrongHold;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller, Luzien
 */
@AIName("gravity")
public class GravityAI extends NpcAI {

	private Future<?> task;

	public GravityAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				AIActions.useSkill(GravityAI.this, 20738);
			}
		}, 0, 3250);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.deleteOwner(GravityAI.this);
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
