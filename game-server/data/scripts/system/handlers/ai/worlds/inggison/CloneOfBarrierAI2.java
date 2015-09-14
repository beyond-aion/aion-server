package ai.worlds.inggison;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;

/**
 * @author Luzien
 */
@AIName("omegaclone")
public class CloneOfBarrierAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleDied() {
		for (VisibleObject object : getKnownList().getKnownObjects().values()) {
			if (object instanceof Npc && isInRange(object, 5)) {
				Npc npc = (Npc) object;
				if (npc.getNpcId() == 216516 && !npc.getLifeStats().isAlreadyDead()) {
					npc.getEffectController().removeEffect(18671);
					break;
				}
			}
		}
		super.handleDied();
	}
}
