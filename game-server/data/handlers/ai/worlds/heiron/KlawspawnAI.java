package ai.worlds.heiron;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;

import ai.GeneralNpcAI;

/**
 * @author cheatkiller
 */
@AIName("klawspawn")
public class KlawspawnAI extends GeneralNpcAI {

	public KlawspawnAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		Npc npc = getOwner().getPosition().getWorldMapInstance().getNpc(212120);
		if (npc == null) {
			if (Rnd.chance() < 10) {
				spawn(212120, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
				AIActions.die(this, creature);
			}
		}
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 1;
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		AIActions.deleteOwner(this);
	}
}
