package ai.siege;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Source
 */
@AIName("siege_mine")
public class MineAI2 extends SiegeNpcAI2 {

	@Override
	protected void handleCreatureAggro(Creature creature) {

		AI2Actions.useSkill(this, 18407);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.deleteOwner(MineAI2.this);
			}

		}, 1500);
	}

}
