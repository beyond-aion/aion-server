package ai.instance.rentusBase;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

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
		if (getCreator() instanceof Npc creator) // Kuhara 217311 (Rentus Base) or 236298 (Occupied Rentus Base)
			boss = creator;
	}

	@Override
	protected void handleMoveArrived() {
		if (isDestroyed.compareAndSet(false, true)) {
			if (boss != null && !boss.isDead()) {
				SkillEngine.getInstance().getSkill(getOwner(), 19659, 60, boss).useNoAnimationSkill();
			}
			ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), 1500);
		}
	}
}
