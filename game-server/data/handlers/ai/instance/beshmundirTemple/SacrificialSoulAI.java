package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.GeneralNpcAI;

/**
 * @author Luzien
 */
@AIName("templeSoul")
public class SacrificialSoulAI extends GeneralNpcAI {

	private Npc boss;

	public SacrificialSoulAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		AIActions.useSkill(this, 18901);
		this.setStateIfNot(AIState.FOLLOWING);
		boss = getPosition().getWorldMapInstance().getNpc(216263);
		if (boss != null && !boss.isDead()) {
			AIActions.targetCreature(this, boss);
			getMoveController().moveToTargetObject();
		}
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (creature.getEffectController().hasAbnormalEffect(18959)) {
			getMoveController().abortMove();
			AIActions.deleteOwner(this);
		}
	}

	@Override
	protected void handleMoveArrived() {
		if (boss != null && !boss.isDead()) {
			SkillEngine.getInstance().getSkill(getOwner(), 18960, 55, boss).useNoAnimationSkill();
			AIActions.deleteOwner(this);
		}
	}

	@Override
	public boolean canThink() {
		return false;
	}
}
