package ai.walkers;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;

import ai.GeneralNpcAI;

/**
 * @author Rolandas
 */
@AIName("generalrunner")
public class WalkGeneralRunnerAI extends GeneralNpcAI {

	public WalkGeneralRunnerAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		getOwner().setState(CreatureState.WEAPON_EQUIPPED);
	}
}
