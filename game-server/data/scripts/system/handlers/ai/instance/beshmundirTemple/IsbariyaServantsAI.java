package ai.instance.beshmundirTemple;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Luzien TODO: Random aggro switch
 */
@AIName("isbariyaServants")
public class IsbariyaServantsAI extends AggressiveNpcAI {

	public IsbariyaServantsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		int lifetime = (getNpcId() == 281659 ? 20000 : 10000);
		toDespawn(lifetime);
	}

	private void toDespawn(int delay) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.deleteOwner(IsbariyaServantsAI.this);
			}
		}, delay);
	}
}
