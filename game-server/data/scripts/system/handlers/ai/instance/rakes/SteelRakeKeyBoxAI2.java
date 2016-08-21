package ai.instance.rakes;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.ChestAI2;

/**
 * @author xTz
 */
@AIName("steel_rake_key_box")
public class SteelRakeKeyBoxAI2 extends ChestAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead() && getOwner().isSpawned()) {
					AI2Actions.deleteOwner(SteelRakeKeyBoxAI2.this);
				}
			}

		}, 180000);
	}
}
