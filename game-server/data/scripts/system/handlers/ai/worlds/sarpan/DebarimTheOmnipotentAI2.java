package ai.worlds.sarpan;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Luzien note: closing door 860 -> collapse room TODO: sniff fight AI
 */
@AIName("debarim")
public class DebarimTheOmnipotentAI2 extends AggressiveNpcAI2 {

	private boolean isStart;

	@Override
	public void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (creature instanceof Player && !isStart) {
			isStart = true;
			getPosition().getWorldMapInstance().getDoors().get(480).setOpen(true); // bugged door, displayed state is the opposite
		}
	}

	@Override
	public void handleDied() {
		super.handleDied();
		getPosition().getWorldMapInstance().getDoors().get(480).setOpen(false);
	}

	@Override
	public void handleBackHome() {
		super.handleBackHome();
		isStart = false;
		getPosition().getWorldMapInstance().getDoors().get(480).setOpen(false);
	}
}
