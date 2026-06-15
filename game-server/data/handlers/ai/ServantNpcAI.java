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
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.SkillType;
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
			ThreadPoolManager.getInstance().schedule(() -> {
				if (getOwner().getNpcObjectType() != NpcObjectType.TOTEM)
					AIActions.targetCreature(ServantNpcAI.this, (Creature) getCreator().getTarget());
				else
					AIActions.targetSelf(ServantNpcAI.this);
				healOrAttack();
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
			case 833403, 833404, 833478, 833479, 833480, 833481 -> duration = 5000;
			// Battle Banner
			case 833077, 833078, 833452, 833453, 833454, 833455 -> {
				duration = 3000;
				startDelay = 100;
			}
		}
		final Creature target = (Creature) getOwner().getTarget();
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (target == null || target.isDead()) {
				AIActions.deleteOwner(ServantNpcAI.this);
				cancelTask();
			} else {
				SkillTemplate template = skill.getTemplate().getSkillTemplate();
				if ((template.getType() != SkillType.MAGICAL || !getOwner().getEffectController().isAbnormalSet(AbnormalState.SILENCE))
					&& (template.getType() != SkillType.PHYSICAL || !getOwner().getEffectController().isAbnormalSet(AbnormalState.BIND))
					&& (!getOwner().getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE))
					&& (!getOwner().isTransformed() || getOwner().getTransformModel().getBanUseSkills() != 1)) {
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
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
