package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author M.O.G. Dision
 * @reworked Estrayl
 */
@AIName("dainatoum_mine")
public class DainatoumBombAI extends AggressiveNpcAI {

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
								getOwner().getController().delete();
						}
					}, 3500);
				}
			}
		}, 4000);
	}
}
