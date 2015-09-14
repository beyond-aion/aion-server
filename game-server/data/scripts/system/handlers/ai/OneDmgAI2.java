package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author xTz
 */
@AIName("one_dmg")
public class OneDmgAI2 extends AggressiveNpcAI2 {

	@Override
	public int modifyDamage(Creature creature, int damage) {
		return 1;
	}

	@Override
	public int modifyOwnerDamage(int damage) {
		return 1;
	}

}
