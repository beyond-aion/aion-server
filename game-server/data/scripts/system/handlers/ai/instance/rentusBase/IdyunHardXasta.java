package ai.instance.rentusBase;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;

/**
 * @author Estrayl
 */
@AIName("idyun_hard_xasta")
public class IdyunHardXasta extends GeneralNpcAI2 {

	private AtomicBoolean isDestinationReached = new AtomicBoolean(false);
	
	@Override
	public boolean canThink() {
		return false;
	}
	
	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getMoveController().isStop()) {
			if (isDestinationReached.compareAndSet(false, true))
				getOwner().getPosition().getWorldMapInstance().getInstanceHandler().onSpecialEvent(getOwner());
		}
	}
}
