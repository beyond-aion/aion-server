package ai;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.NpcObjectType;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
@AIName("servant")
public class ServantNpcAI extends GeneralNpcAI {

	private Future<?> skillTask;

	public ServantNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	public void think() {
		// servants are not thinking
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getCreator() != null) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (getOwner().getNpcObjectType() != NpcObjectType.TOTEM)
						AIActions.targetCreature(ServantNpcAI.this, (Creature) getCreator().getTarget());
					else
						AIActions.targetSelf(ServantNpcAI.this);
					healOrAttack();
				}
			}, 200);
		}
	}

	private void healOrAttack() {
		NpcSkillEntry skill = getSkillList().getRandomSkill();
		if (skill == null)
			return;
		getOwner().getGameStats().setLastSkill(skill);
		int duration = getOwner().getNpcObjectType() == NpcObjectType.TOTEM ? 3000 : 5000;
		int startDelay = 1000;
		switch (getOwner().getNpcId()) {
			// Taunting Spirit
			case 833403:
			case 833404:
			case 833478:
			case 833479:
			case 833480:
			case 833481:
				duration = 5000;
				startDelay = 1000;
				break;
			// Battle Banner
			case 833077:
			case 833078:
			case 833452:
			case 833453:
			case 833454:
			case 833455:
				duration = 3000;
				startDelay = 100;
				break;
		}
		final Creature target = (Creature) getOwner().getTarget();
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (target == null || target.isDead()) {
					AIActions.deleteOwner(ServantNpcAI.this);
					cancelTask();
				} else {
					SkillEngine.getInstance().getSkill(getOwner(), skill.getSkillId(), skill.getSkillLevel(), getOwner().getTarget()).useSkill();
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
