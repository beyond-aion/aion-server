package ai.instance.drakenspire;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI2;

/**
 * @author Estrayl
 */
@AIName("wave_event_executor")
public class WaveEventExecutorAI2 extends GeneralNpcAI2 {

	private AtomicBoolean isDestinationReached = new AtomicBoolean(false);

	@Override
	public void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getMoveController().isStop()) {
			if (isDestinationReached.compareAndSet(false, true)) {
				getOwner().getSpawn().setWalkerId("");
				PacketSendUtility.broadcastMessage(getOwner(), 1501318, 0);
				scheduleSummon();
			}
		}
	}

	private void scheduleSummon() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				SkillEngine.getInstance().getSkill(getOwner(), 20839, 1, getOwner()).useSkill();
				PacketSendUtility.broadcastMessage(getOwner(), 1501317, 2000);
			}
		}, 3500);
	}
}
