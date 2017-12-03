package ai.instance.drakenspire;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("wave_event_executor")
public class WaveEventExecutorAI extends GeneralNpcAI {

	private AtomicBoolean isDestinationReached = new AtomicBoolean();

	public WaveEventExecutorAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getMoveController().isStop() && isDestinationReached.compareAndSet(false, true)) {
			getOwner().getSpawn().setWalkerId("");
			PacketSendUtility.broadcastMessage(getOwner(), 1501318);
			scheduleSummon();
		}
	}

	private void scheduleSummon() {
		ThreadPoolManager.getInstance().schedule(() -> {
			SkillEngine.getInstance().getSkill(getOwner(), 20839, 1, getOwner()).useSkill();
			PacketSendUtility.broadcastMessage(getOwner(), 1501317, 2000);
		}, 3500);
	}
}
