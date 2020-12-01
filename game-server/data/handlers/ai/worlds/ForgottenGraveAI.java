package ai.worlds;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.GeneralNpcAI;

/**
 * @author Tibald
 */
@AIName("forgottengrave")
public class ForgottenGraveAI extends GeneralNpcAI {

	private AtomicBoolean isSpawned = new AtomicBoolean(false);

	public ForgottenGraveAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		if (isSpawned.compareAndSet(false, true)) {
			rndSpawnInRange(283906, 1, 2);
			rndSpawnInRange(283906, 1, 2);
		}
	}
}
