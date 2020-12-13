package ai.instance.danuarSanctuary;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;

import ai.GeneralNpcAI;

/**
 * @author Tibald
 */
@AIName("ancientdanuarcoffin")
public class AncientDanuarCoffinAI extends GeneralNpcAI {

	public AncientDanuarCoffinAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 1;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		if (Rnd.chance() < 40) {
			spawn(233085, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
		}
	}
}
