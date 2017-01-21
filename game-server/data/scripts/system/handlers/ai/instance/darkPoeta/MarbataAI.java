package ai.instance.darkPoeta;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("marbata")
public class MarbataAI extends AggressiveNpcAI {

	private boolean isStart;

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (!isStart) {
			isStart = true;
			buff();
		}
	}

	private void buff() {
		AIActions.useSkill(this, 18556);
		AIActions.useSkill(this, 18110);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		isStart = false;
		getEffectController().removeEffect(18556);
		getEffectController().removeEffect(18110);
	}
}
