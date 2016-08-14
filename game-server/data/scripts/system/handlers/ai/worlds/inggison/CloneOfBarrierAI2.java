package ai.worlds.inggison;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;

import ai.AggressiveNpcAI2;

/**
 * @author Luzien
 * @modified Neon
 */
@AIName("omegaclone")
public class CloneOfBarrierAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleDied() {
		VisibleObject object = getKnownList().findObject(216516); // Omega
		if (object instanceof Npc && !((Npc) object).getLifeStats().isAlreadyDead() && isInRange(object, 5))
			((Npc) object).getEffectController().removeEffect(18671);
		super.handleDied();
	}
}
