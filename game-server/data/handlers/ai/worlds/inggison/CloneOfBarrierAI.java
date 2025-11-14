package ai.worlds.inggison;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

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
	protected void handleSpawned() {
		super.handleSpawned();
		// delay for spawn animation and because KnownList isn't initialized yet
		ThreadPoolManager.getInstance().schedule(() -> {
			if (getKnownList().getObject(getCreatorId()) instanceof Npc omega) {
				getOwner().setTarget(omega);
				AIActions.useSkill(this, 18671);
			}
		}, 3000);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		if (getKnownList().getObject(getCreatorId()) instanceof Npc omega)
			omega.getEffectController().removeEffect(18671);
	}
}
