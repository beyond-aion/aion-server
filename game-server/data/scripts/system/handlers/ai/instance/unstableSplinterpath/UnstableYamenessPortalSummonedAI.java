package ai.instance.unstableSplinterpath;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 * @edit Cheatkiller
 */

@AIName("unstableyamenessportal")
public class UnstableYamenessPortalSummonedAI extends AggressiveNpcAI {

	@Override
	protected void handleSpawned() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				spawnSummons();
			}
		}, 12000);
	}

	private void spawnSummons() {
		if (getOwner() != null) {
			spawn(219565, getOwner().getX() + 3, getOwner().getY() - 3, getOwner().getZ(), (byte) 0);
			spawn(219566, getOwner().getX() - 3, getOwner().getY() + 3, getOwner().getZ(), (byte) 0);
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					if (!isAlreadyDead() && getOwner() != null) {
						spawn(219565, getOwner().getX() + 3, getOwner().getY() - 3, getOwner().getZ(), (byte) 0);
						spawn(219566, getOwner().getX() - 3, getOwner().getY() + 3, getOwner().getZ(), (byte) 0);
					}
				}
			}, 60000);
		}
	}
}
