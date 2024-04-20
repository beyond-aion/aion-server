package ai;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats
 */
@AIName("conquest_offering_buff_npc")
public class ConquestOfferingBuffNpcAI extends ActionItemNpcAI {

	private AtomicBoolean used = new AtomicBoolean(false);
	private Future<?> despawnTask;

	public ConquestOfferingBuffNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		sendWakeUpMsg();
		despawnTask = ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().delete(), 65000);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (used.compareAndSet(false, true)) {
			sendTalkedMsg();
			int skillId = 21924 + Rnd.get(0, 3);
			SkillEngine.getInstance().getSkill(getOwner(), skillId, 1, player).useSkill();
			getOwner().getController().delete();
		}
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
		if (despawnTask != null && !despawnTask.isDone())
			despawnTask.cancel(true);
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
