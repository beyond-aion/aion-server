package ai.instance.unstableSplinterpath;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Luzien
 */
@AIName("unstablepazuzuworm")
public class UnstablePazuzuWormAI extends AggressiveNpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.targetCreature(UnstablePazuzuWormAI.this, getPosition().getWorldMapInstance().getNpc(219554));
				AIActions.useSkill(UnstablePazuzuWormAI.this, 19291);
			}

		}, 3000);
	}
}
