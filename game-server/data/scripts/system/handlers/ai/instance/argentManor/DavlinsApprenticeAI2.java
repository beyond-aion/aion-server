package ai.instance.argentManor;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.manager.WalkManager;

/**
 * @author xTz
 */
@AIName("davlins_apprentice")
public class DavlinsApprenticeAI2 extends GeneralNpcAI2 {

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		getSpawnTemplate().setWalkerId(null);
		super.handleSpawned();
	}

	@Override
	protected void handleMoveArrived() {
		int point = getOwner().getMoveController().getCurrentPoint();
		super.handleMoveArrived();
		if (point == 5) {
			getSpawnTemplate().setWalkerId(null);
			WalkManager.stopWalking(this);
			AI2Actions.deleteOwner(this);
		}
	}

}
