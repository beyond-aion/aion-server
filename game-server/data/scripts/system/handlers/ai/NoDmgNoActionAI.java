package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Yeats on 17.03.2016
 */
@AIName("no_dmg_no_action")
public class NoDmgNoActionAI extends NpcAI {

	@Override
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
		return 0;
	}
}
