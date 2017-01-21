package ai.siege;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Source
 */
@AIName("siege_mine")
public class MineAI extends SiegeNpcAI {

	@Override
	protected void handleCreatureAggro(Creature creature) {

		AIActions.useSkill(this, 18407);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.deleteOwner(MineAI.this);
			}

		}, 1500);
	}

}
