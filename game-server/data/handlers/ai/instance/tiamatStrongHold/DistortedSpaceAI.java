package ai.instance.tiamatStrongHold;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("distortedspace")
public class DistortedSpaceAI extends NpcAI {

	private Future<?> task;

	public DistortedSpaceAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		useskill();
	}

	private void useskill() {
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (getOwner().getNpcId() == 283097)
				AIActions.useSkill(DistortedSpaceAI.this, 20740);
		}, 500, 2000);

		ThreadPoolManager.getInstance().schedule(() -> {
			cancelTask();
			if (getOwner().getNpcId() == 283097)
				AIActions.useSkill(DistortedSpaceAI.this, 20742);
			getOwner().getController().die();
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
		AIActions.deleteOwner(this);
	}

	@Override
	public void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
