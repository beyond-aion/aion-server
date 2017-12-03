package ai.instance.rentusBase;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("idyun_hard_xasta")
public class IdyunHardXasta extends GeneralNpcAI {

	private AtomicBoolean isDestinationReached = new AtomicBoolean(false);

	public IdyunHardXasta(Npc owner) {
		super(owner);
	}

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
