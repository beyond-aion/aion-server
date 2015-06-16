package ai.walkers;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;


/**
 * @author Rolandas
 *
 */
@AIName("aggrorunner")
public class WalkAggroRunnerAI2 extends AggressiveNpcAI2 {
	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		getOwner().setState(CreatureState.WEAPON_EQUIPPED);
	}
}
