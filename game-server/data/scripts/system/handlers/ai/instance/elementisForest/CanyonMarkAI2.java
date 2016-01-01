package ai.instance.elementisForest;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author Luzien
 */
@AIName("canyonmark")
public class CanyonMarkAI2 extends AggressiveNpcAI2 {

	private Creature target;

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		markTarget();
	}

	private void markTarget() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				target = (Creature) getOwner().getTarget();
				if (target != null) {
					AI2Actions.useSkill(CanyonMarkAI2.this, 19504);

					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (!isAlreadyDead()) {
								AI2Actions.targetCreature(CanyonMarkAI2.this, target);
								AI2Actions.useSkill(CanyonMarkAI2.this, 19505);
								AI2Actions.deleteOwner(CanyonMarkAI2.this);
							}
						}

					}, Rnd.get(5, 10) * 1000);

				} else
					AI2Actions.deleteOwner(CanyonMarkAI2.this);
			}
		}, 5000);
	}
}
