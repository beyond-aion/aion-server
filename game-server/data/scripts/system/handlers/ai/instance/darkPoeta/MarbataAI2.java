package ai.instance.darkPoeta;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author xTz
 */
@AIName("marbata")
public class MarbataAI2 extends AggressiveNpcAI2 {

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
		AI2Actions.useSkill(this, 18556);
		AI2Actions.useSkill(this, 18110);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		isStart = false;
		getEffectController().removeEffect(18556);
		getEffectController().removeEffect(18110);
	}
}
