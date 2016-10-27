package ai.siege;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Bobobear
 */
@AIName("base_flag")
public class FlagBaseNpcAI2 extends NpcAI2 {

	@Override
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
		return 0;
	}

	@Override
	public int modifyOwnerDamage(int damage) {
		return 0;
	}
}
