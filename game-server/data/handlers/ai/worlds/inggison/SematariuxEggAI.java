package ai.worlds.inggison;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("sematariux_egg")
public class SematariuxEggAI extends NpcAI {

	private Future<?> spawnTask;

	public SematariuxEggAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		spawnTask = ThreadPoolManager.getInstance().schedule(() -> spawn(281456, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0), 4,
			TimeUnit.MINUTES);
	}

	@Override
	protected void handleDied() {
		for (VisibleObject vo : getKnownList().getKnownObjects().values()) {
			if (vo instanceof Npc npc && npc.getNpcId() == 216520) {
				npc.getEffectController().removeEffect(18726);
				npc.queueSkill(19199, 1, 3000);
				break;
			}
		}
		cancelSpawnTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelSpawnTask();
		super.handleDespawned();
	}

	private void cancelSpawnTask() {
		if (spawnTask != null && !spawnTask.isCancelled())
			spawnTask.cancel(true);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, REWARD_LOOT, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
