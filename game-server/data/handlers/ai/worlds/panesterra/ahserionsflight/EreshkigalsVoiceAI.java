package ai.worlds.panesterra.ahserionsflight;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("ereshkigals_voice")
public class EreshkigalsVoiceAI extends NpcAI {

	private Future<?> idleTask;

	public EreshkigalsVoiceAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		idleTask = ThreadPoolManager.getInstance().schedule(() -> {
			PacketSendUtility.broadcastMessage(getOwner(), 1501160); // I won't forgive you, Fregion!!!
			getOwner().getController().addTask(TaskId.DESPAWN,
				ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().deleteIfAliveOrCancelRespawn(), 2000));
		}, 30000);
	}

	@Override
	protected void handleDespawned() {
		if (idleTask != null && !idleTask.isDone())
			idleTask.cancel(true);
		super.handleDespawned();
	}
}
