package ai.instance.danuarReliquary;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 */
@AIName("malicious_ice_storm")
public class MaliciousIceStormAI extends NpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		iceStorm();
	}

	private void iceStorm() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.useSkill(MaliciousIceStormAI.this, 21180);
				despawn();
			}
		}, 100);
	}

	private void despawn() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				getOwner().getController().delete();
			}
		}, 2100);
	}
}
