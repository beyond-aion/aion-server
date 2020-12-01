package ai.walkers;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;

import ai.AggressiveNpcAI;

/**
 * @author Rolandas
 */
@AIName("aggrorunner")
public class WalkAggroRunnerAI extends AggressiveNpcAI {

	public WalkAggroRunnerAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		getOwner().setState(CreatureState.WEAPON_EQUIPPED);
	}
}
