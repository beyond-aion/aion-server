package ai.instance.danuarSanctuary;

import ai.GeneralNpcAI2;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author Tibald
 */
@AIName("ancientdanuarcoffin")
public class AncientDanuarCoffinAI2 extends GeneralNpcAI2 {

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	public int modifyDamage(Creature creature, int damage) {
		return 1;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		if (Rnd.get(0, 100) < 40) {
			spawn(233085, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
		}
	}
}
