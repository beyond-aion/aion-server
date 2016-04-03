package ai;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.NpcObjectType;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.skillengine.SkillEngine;
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
		int startDelay = 1000;
		switch (getOwner().getNpcId()) {
			//Taunting Spirit
			case 833403:
			case 833404:
			case 833478:
			case 833479:
			case 833480:
			case 833481:
				duration = 5000;
				startDelay = 1000;
				break;
			//Battle Banner
			case 833077:
			case 833078:
			case 833452:
			case 833453:
			case 833454:
			case 833455:
				duration = 3000;
				startDelay = 100;
				break;
				default:
					break;
		}
		final Creature target = (Creature) getOwner().getTarget();
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (target == null || target.getLifeStats().isAlreadyDead()) {
					AI2Actions.deleteOwner(ServantNpcAI2.this);
					cancelTask();
				} else {
					SkillEngine.getInstance().getSkill(getOwner(), skillId, skillLevel, getOwner().getTarget()).useSkill();
					//getOwner().getController().useSkill(skillId, skillLevel);
				}
			}
		}, startDelay, duration);
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
