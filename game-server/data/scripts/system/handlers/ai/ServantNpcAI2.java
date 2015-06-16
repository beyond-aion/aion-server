package ai;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.NpcObjectType;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@AIName("servant")
public class ServantNpcAI2 extends GeneralNpcAI2 {
	
	private Future<?> skillTask;

	@Override
	public void think() {
		// servants are not thinking
	}
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getCreator() != null) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					if (getOwner().getNpcObjectType() != NpcObjectType.TOTEM)
						AI2Actions.targetCreature(ServantNpcAI2.this, (Creature) getCreator().getTarget());
					else 
						AI2Actions.targetSelf(ServantNpcAI2.this);
					healOrAttack();
				}
			}, 200);
		}
	}
	
	
	private void healOrAttack() {
		if (skillId == 0) {
			NpcSkillEntry npcSkill = getSkillList().getRandomSkill();
			if (npcSkill == null)
				return;
			skillId = npcSkill.getSkillId();
			skillLevel = npcSkill.getSkillLevel();
		}
		int duration = getOwner().getNpcObjectType() == NpcObjectType.TOTEM ? 3000 : 5000;
		final Creature target = (Creature) getOwner().getTarget();
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (target == null || target.getLifeStats().isAlreadyDead()) {
					AI2Actions.deleteOwner(ServantNpcAI2.this);
					cancelTask();
				}
				else
					getOwner().getController().useSkill(skillId, skillLevel);
			}
		}, 1000, duration);
		getOwner().getController().addTask(TaskId.SKILL_USE, skillTask);
	}

	@Override
	public boolean isMoveSupported() {
		return false;
	}
	
	private void cancelTask() {
		if (skillTask != null && !skillTask.isDone())
			skillTask.cancel(true);
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
