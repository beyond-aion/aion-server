package ai.instance.muadasTrencher;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author xTz
 */
@AIName("shirik_regulator")
public class ShirikRegulatorAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleDied() {
		WorldPosition p = getPosition();
		if (p != null) {
			spawn(282539, p.getX(), p.getY(), p.getZ(), (byte) 0);
		}
		super.handleDied();
		AI2Actions.deleteOwner(this);
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}
}
