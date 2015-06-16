package ai.walkers;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;


/**
 * @author Rolandas
 *
 */
@AIName("generalrunner")
public class WalkGeneralRunnerAI2 extends GeneralNpcAI2 {
	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		getOwner().setState(CreatureState.WEAPON_EQUIPPED);
	}
}
