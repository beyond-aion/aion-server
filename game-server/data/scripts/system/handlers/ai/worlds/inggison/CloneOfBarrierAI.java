package ai.worlds.inggison;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;

import ai.AggressiveNpcAI;

/**
 * @author Luzien
 * @modified Neon
 */
@AIName("omegaclone")
public class CloneOfBarrierAI extends AggressiveNpcAI {

	@Override
	protected void handleDied() {
		VisibleObject object = getKnownList().findObject(216516); // Omega
		if (object instanceof Npc && !((Npc) object).getLifeStats().isAlreadyDead() && isInRange(object, 5))
			((Npc) object).getEffectController().removeEffect(18671);
		super.handleDied();
	}
}
