package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Yeats on 17.03.2016
 */
@AIName("no_dmg_no_action")
public class NoDmgNoActionAI2 extends NpcAI2 {

	@Override
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
		return 0;
	}
}
