package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author xTz
 */
@AIName("onedmgperhit")
public class OneDmgPerHitAI2 extends NoActionAI2 {

	@Override
	public int modifyDamage(Creature creature, int damage) {
		return 1;
	}
}
