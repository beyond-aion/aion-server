package ai.instance.danuarReliquary;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 */
@AIName("vengeful_orb")
public class VengefulOrbAI extends NpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		vengefulOrb();
	}

	private void vengefulOrb() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.useSkill(VengefulOrbAI.this, 21178);
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
		}, 13000);
	}
}
