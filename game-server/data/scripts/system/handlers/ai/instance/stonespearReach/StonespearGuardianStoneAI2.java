package ai.instance.stonespearReach;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats on 20.02.2016.
 */
@AIName("stonespear_guardian_stone")
public class StonespearGuardianStoneAI2 extends NpcAI2 {

	private Future<?> task;

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		startTask();
	}

	private void startTask() {
		task = ThreadPoolManager.getInstance().schedule(() -> {
			if (getOwner() != null) {
				PacketSendUtility.broadcastPacket(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_OBJ_End());
				getOwner().getController().onDelete();
			}
		}, 55000); // message says 2mins but its actually only ~1min.
	}

	@Override
	public void handleDied() {
		super.handleDied();
		getOwner().getController().onDelete();
		if (task != null && !task.isCancelled()) {
			task.cancel(true);
		}
	}

	@Override
	public void handleDespawned() {
		if (task != null && !task.isCancelled()) {
			task.cancel(true);
		}
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
			case SHOULD_LOOT:
				return false;
			default:
				return super.ask(question);
		}
	}
}
