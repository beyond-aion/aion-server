package ai.worlds.inggison;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;

import ai.AggressiveNpcAI;

/**
 * @author Luzien
 */
@AIName("omegaclone")
public class CloneOfBarrierAI extends AggressiveNpcAI {

	public CloneOfBarrierAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDied() {
		VisibleObject object = getKnownList().findObject(216516); // Omega
		if (object instanceof Npc omega && !omega.isDead() && isInRange(omega, 5))
			omega.getEffectController().removeEffect(18671);
		super.handleDied();
	}
}
