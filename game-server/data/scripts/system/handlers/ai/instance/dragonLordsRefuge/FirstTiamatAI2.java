package ai.instance.dragonLordsRefuge;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;

/**
 * @author Cheatkiller
 */
@AIName("firsttiamat")
public class FirstTiamatAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleDeactivate() {
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getNpcId() == 219360)
			AI2Actions.useSkill(this, 20917);
	}
}
