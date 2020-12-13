package ai.worlds.levinshor;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.handler.TargetEventHandler;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Yeats
 */
@AIName("LDF4_Advance_Ancient_Monster")
public class AncientMonsterAI extends AggressiveNpcAI {

	public AncientMonsterAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		getOwner().getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead())
				getOwner().getController().delete();
		}, 1000 * 60 * 60));
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getDistanceToSpawnLocation() > 15)
			TargetEventHandler.onTargetGiveup(this);
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		float multi = 1.5f;
		if (effect != null && effect.getSkillTemplate().getSkillId() == 21780)
			multi = 3;
		return super.modifyOwnerDamage(damage * multi, effected, effect);
	}
}
