package ai.instance.unstableSplinterpath;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI2;

/**
 * @author Luzien
 */
@AIName("unstablepazuzuworm")
public class UnstablePazuzuWormAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.targetCreature(UnstablePazuzuWormAI2.this, getPosition().getWorldMapInstance().getNpc(219554));
				AI2Actions.useSkill(UnstablePazuzuWormAI2.this, 19291);
			}

		}, 3000);
	}
}
