package ai.instance.rentusBase;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 *
 * @author xTz
 */
@AIName("kuhara_bomb")
public class KuharaBombAI2 extends GeneralNpcAI2 {

	private AtomicBoolean isDestroyed = new AtomicBoolean(false);
	private Npc boss;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		this.setStateIfNot(AIState.FOLLOWING);
		boss = getPosition().getWorldMapInstance().getNpc(217311);
	}

	@Override
	protected void handleMoveArrived() {
		if (isDestroyed.compareAndSet(false, true)) {
			if (boss != null && !NpcActions.isAlreadyDead(boss)) {
				SkillEngine.getInstance().getSkill(getOwner(), 19659, 60, boss).useNoAnimationSkill();
			}
		}
	}
}