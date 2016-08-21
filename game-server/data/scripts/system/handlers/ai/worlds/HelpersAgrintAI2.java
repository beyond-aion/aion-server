package ai.worlds;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

import ai.AggressiveNpcAI2;

/**
 * @author xTz
 */
@AIName("helpers_agrint")
public class HelpersAgrintAI2 extends AggressiveNpcAI2 {

	@Override
	public int modifyOwnerDamage(int damage) {
		return 1;
	}

	@Override
	public int modifyDamage(Creature creature, int damage) {
		return 1;
	}

}
