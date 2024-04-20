package ai.instance.stonespearReach;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Yeats
 */
@AIName("stonespear_guardian_stone")
public class StonespearGuardianStoneAI extends NpcAI {

	private Future<?> task;

	public StonespearGuardianStoneAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		startTask();
	}

	private void startTask() {
		task = ThreadPoolManager.getInstance().schedule(() -> {
			PacketSendUtility.broadcastPacket(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_OBJ_End());
			getOwner().getController().delete();
		}, 55000); // message says 2mins but its actually only ~1min.
	}

	@Override
	public void handleDied() {
		super.handleDied();
		getOwner().getController().delete();
		if (task != null && !task.isCancelled())
			task.cancel(true);
	}

	@Override
	public void handleDespawned() {
		if (task != null && !task.isCancelled())
			task.cancel(true);
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT, REWARD_LOOT, ALLOW_DECAY -> false;
			default -> super.ask(question);
		};
	}
}
