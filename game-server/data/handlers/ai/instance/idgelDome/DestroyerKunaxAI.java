package ai.instance.idgelDome;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AttackIntention;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PositionUtil;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("destroyer_kunax")
public class DestroyerKunaxAI extends AggressiveNpcAI {

	public DestroyerKunaxAI(Npc owner) {
		super(owner);
	}

	@Override
	public AttackIntention chooseAttackIntention() {
		double dist = 0;
		if (getTarget() != null) {
			dist = PositionUtil.getDistance(getOwner(), getTarget()) - getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide()
				- getTarget().getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide();
		}
		if (dist > 3 && dist <= 30) {
			SkillEngine.getInstance().getSkill(getOwner(), 21550, 56, getTarget()).useSkill();
			return AttackIntention.SKILL_ATTACK;
		}
		return super.chooseAttackIntention();
	}
}
