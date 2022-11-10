package ai.worlds;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Tibald
 */
@AIName("forgottengrave")
public class ForgottenGraveAI extends GeneralNpcAI {

	private final AtomicBoolean isSpawned = new AtomicBoolean();

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
			scheduleAddDespawn((Npc) rndSpawnInRange(283906, 1, 2));
			scheduleAddDespawn((Npc) rndSpawnInRange(283906, 1, 2));
		}
	}

	private void scheduleAddDespawn(Npc npc) {
		npc.getController().addTask(TaskId.DESPAWN,
			ThreadPoolManager.getInstance().schedule(() -> npc.getController().deleteIfAliveOrCancelRespawn(), 5, TimeUnit.MINUTES));
	}
}
