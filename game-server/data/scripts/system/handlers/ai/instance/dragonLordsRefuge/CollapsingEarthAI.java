package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("collapsingearth")
public class CollapsingEarthAI extends NpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		useskill();
	}

	private void useskill() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.useSkill(CollapsingEarthAI.this, 20173);
				getOwner().getController().die();
			}
		}, 3000);
	}
}
