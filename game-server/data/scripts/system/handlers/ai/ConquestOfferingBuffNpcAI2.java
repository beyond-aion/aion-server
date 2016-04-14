package ai;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats 16.03.2016.
 */
@AIName("conquest_offering_buff_npc")
public class ConquestOfferingBuffNpcAI2 extends ActionItemNpcAI2 {

	private AtomicBoolean used = new AtomicBoolean(false);
	private Future<?> despawnTask;

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		sendWakeUpMsg();
		startDespawnTask();
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (used.compareAndSet(false, true)) {
			sendTalkedMsg();
			int skillId = 21924 + Rnd.get(0, 3);
			SkillEngine.getInstance().getSkill(getOwner(), skillId, 1, player).useSkill();
			getOwner().getController().onDelete();
		}
	}

	private void startDespawnTask() {
		despawnTask = ThreadPoolManager.getInstance().schedule((Runnable) () -> {
			if (getOwner() != null)
				getOwner().getController().onDelete();
		}, 65000);
	}

	@Override
	public void handleDied() {
		super.handleDied();
		cancelTask();
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	private void cancelTask() {
		if (despawnTask != null && !despawnTask.isCancelled()) {
			despawnTask.cancel(true);
		}
	}

	private void sendWakeUpMsg() {
		int msg = (1501279 + (Rnd.get(0, 2) * 2));
		PacketSendUtility.broadcastMessage(getOwner(), msg, 1500);
	}

	private void sendTalkedMsg() {
		int msg = (1501280 + (Rnd.get(0, 2) * 2));
		PacketSendUtility.broadcastMessage(getOwner(), msg);
	}
}
