package ai.classNpc;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.utils.ThreadPoolManager;


/**
 * @author Cheatkiller
 *
 */
@AIName("enemyservant")
public class EnemyServantAI2 extends NpcAI2 {
	
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if(getCreator() == null || getCreator().getTarget() == null)
					return;
				AI2Actions.targetCreature(EnemyServantAI2.this, (Creature) getCreator().getTarget());
				attack();
			}
		}, 2000);
	}
		
	private void attack() {
		Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				getOwner().getController().useSkill(16907, 55);
			}
		}, 1000, 6000);
		getOwner().getController().addTask(TaskId.SKILL_USE, task);
	}
	
	@Override
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
				return AIAnswers.NEGATIVE;
			case SHOULD_RESPAWN:
				return AIAnswers.NEGATIVE;
			case SHOULD_REWARD:
				return AIAnswers.NEGATIVE;
			default:
				return null;
		}
	}
}

