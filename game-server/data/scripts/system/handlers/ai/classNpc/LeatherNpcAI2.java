package ai.classNpc;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.model.gameobjects.state.CreatureSeeState;

/**
 * @author Whoop
 */
public class LeatherNpcAI2 extends AggressiveNpcAI2 {

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		getOwner().setSeeState(CreatureSeeState.SEARCH2);
		calculatePdef();
	}

	private void calculatePdef() {

	}
}
