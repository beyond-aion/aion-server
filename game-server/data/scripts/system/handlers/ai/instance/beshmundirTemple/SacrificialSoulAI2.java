package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.GeneralNpcAI2;

/**
 * @author Luzien
 */
@AIName("templeSoul")
public class SacrificialSoulAI2 extends GeneralNpcAI2 {

	private Npc boss;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		AI2Actions.useSkill(this, 18901);
		this.setStateIfNot(AIState.FOLLOWING);
		boss = getPosition().getWorldMapInstance().getNpc(216263);
		if (boss != null && !NpcActions.isAlreadyDead(boss)) {
			AI2Actions.targetCreature(this, boss);
			getMoveController().moveToTargetObject();
		}
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (creature.getEffectController().hasAbnormalEffect(18959)) {
			getMoveController().abortMove();
			AI2Actions.deleteOwner(this);
		}
	}

	@Override
	protected void handleMoveArrived() {
		if (boss != null && !NpcActions.isAlreadyDead(boss)) {
			SkillEngine.getInstance().getSkill(getOwner(), 18960, 55, boss).useNoAnimationSkill();
			AI2Actions.deleteOwner(this);
		}
	}

	@Override
	public boolean canThink() {
		return false;
	}
}
