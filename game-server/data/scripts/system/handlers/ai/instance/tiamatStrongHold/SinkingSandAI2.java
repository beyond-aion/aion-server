package ai.instance.tiamatStrongHold;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Cheatkiller
 */
@AIName("sinkingsand")
public class SinkingSandAI2 extends NpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		useskill();
	}

	private void useskill() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.useSkill(SinkingSandAI2.this, 20723);
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						AI2Actions.deleteOwner(SinkingSandAI2.this);
					}
				}, 1000);
			}
		}, 3000);
	}
}
