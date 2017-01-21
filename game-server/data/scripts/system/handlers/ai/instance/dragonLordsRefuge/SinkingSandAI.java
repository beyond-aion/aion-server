package ai.instance.dragonLordsRefuge;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("sinkingsandtiamat")
public class SinkingSandAI extends NpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		useskill();
	}

	private void useskill() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getOwner().getNpcId() == 283136)
					AIActions.useSkill(SinkingSandAI.this, 20965);
				getOwner().getController().die();
			}
		}, 10000);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		AIActions.deleteOwner(this);
	}
}
