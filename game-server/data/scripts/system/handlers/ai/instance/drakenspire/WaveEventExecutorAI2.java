package ai.instance.drakenspire;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

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
				PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(true, 1501318, getOwner().getObjectId(), 1));
				scheduleSummon();
			}
		}
	}

	private void scheduleSummon() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				SkillEngine.getInstance().getSkill(getOwner(), 20839, 1, getOwner()).useSkill();
				ThreadPoolManager.getInstance().schedule(new Runnable() {
					
					@Override
					public void run() {
						PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(true, 1501317, getOwner().getObjectId(), 1));
					}
				}, 2000);
			}
		}, 3500);
	}
}
