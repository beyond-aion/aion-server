package ai.instance.stonespearReach;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Yeats
 */
@AIName("stonespear_kebbit")
public class KebbitAI extends GeneralNpcAI {

	private Future<?> despawnTask;

	public KebbitAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		despawnTask = ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), 15500); // 15,5s
	}

	@Override
	public void handleDied() {
		cancelTask();
		super.handleDied();
		getOwner().getController().delete();
	}

	private void cancelTask() {
		if (despawnTask != null && !despawnTask.isDone())
			despawnTask.cancel(false);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT, REWARD_LOOT, ALLOW_DECAY -> false;
			default -> super.ask(question);
		};
	}

	@Override
	protected void handleAttack(Creature creature) {
		// do nothing
	}
}
