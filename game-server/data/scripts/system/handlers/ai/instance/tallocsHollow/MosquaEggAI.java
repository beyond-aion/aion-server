package ai.instance.tallocsHollow;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("mosquaegg")
public class MosquaEggAI extends AggressiveNpcAI {

	public MosquaEggAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				checkSpawn();
			}
		}, 17000);
	}

	private void checkSpawn() {
		if (getPosition().isSpawned()) {
			// spawn - Spawned Supraklaw
			spawn(217132, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
			AIActions.deleteOwner(this);
		}
	}
}
