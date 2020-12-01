package ai.instance.rentusBase;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.GeneralNpcAI;

/**
 * @author xTz
 */
@AIName("kuhara_bomb")
public class KuharaBombAI extends GeneralNpcAI {

	private AtomicBoolean isDestroyed = new AtomicBoolean(false);
	private Npc boss;

	public KuharaBombAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		this.setStateIfNot(AIState.FOLLOWING);
		boss = getPosition().getWorldMapInstance().getNpc(217311);
	}

	@Override
	protected void handleMoveArrived() {
		if (isDestroyed.compareAndSet(false, true)) {
			if (boss != null && !boss.isDead()) {
				SkillEngine.getInstance().getSkill(getOwner(), 19659, 60, boss).useNoAnimationSkill();
			}
		}
	}
}
