package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AttackIntention;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Homing;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author ATracer
 */
@AIName("homing")
public class HomingNpcAI extends GeneralNpcAI {

	@Override
	public void think() {
		// homings are not thinking to return :)
	}

	@Override
	public AttackIntention chooseAttackIntention() {
		return AttackIntention.SIMPLE_ATTACK;
	}

	@Override
	protected void handleAttackComplete() {
		super.handleAttackComplete();
		Homing owner = (Homing) getOwner();
		if (owner.getSkillList() != null) {
			NpcSkillEntry skill = owner.getSkillList().getRandomSkill();
			if (skill != null) {
				skillId = skill.getSkillId();
				skillLevel = skill.getSkillLevel();
				SkillEngine.getInstance().applyEffectDirectly(skillId, skillLevel, owner, (Creature) owner.getTarget(), 0);
			}
		}
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
