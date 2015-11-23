package ai.instance.illuminaryObelisk;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author M.O.G. Dision
 * @reworked Estrayl
 */
@AIName("dainatoum_mine")
public class DainatoumBombAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		handleExplosion();
	}

	private void handleExplosion() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), 21534, 1, getOwner()).useSkill();
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (!isAlreadyDead())
								getOwner().getController().onDelete();
						}
					}, 3500);
				}
			}
		}, 4000);
	}
}
