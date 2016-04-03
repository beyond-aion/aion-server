package ai.classNpc;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("drakanhealingservant")
public class DrakanHealingServantAI2 extends NpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getCreator() == null)
					return;
				AI2Actions.targetCreature(DrakanHealingServantAI2.this, (Creature) getCreator());
				heal();
			}
		}, 2000);
	}

	private void heal() {
		Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				getOwner().getController().useSkill(20520);
			}
		}, 1000, 6000);
		getOwner().getController().addTask(TaskId.SKILL_USE, task);
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
