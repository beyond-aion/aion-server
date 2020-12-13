package ai.worlds.inggison;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;

import ai.GeneralNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("hugeegg")
public class HugeEggAI extends GeneralNpcAI {

	public HugeEggAI(Npc owner) {
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
		if (Rnd.nextBoolean()) {
			spawn(217097, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
			AIActions.deleteOwner(this);
		}
	}
}
