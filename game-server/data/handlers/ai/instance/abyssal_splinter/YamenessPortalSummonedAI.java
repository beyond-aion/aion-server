package ai.instance.abyssal_splinter;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("yamenessportal")
public class YamenessPortalSummonedAI extends AggressiveNpcAI {

	public YamenessPortalSummonedAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(this::spawnSummons, 12000);
	}

	private void spawnSummons() {
		if (isDead() || !getOwner().isSpawned()) // ensure npc is still alive and instance is not destroyed yet
			return;
		spawn(281903, getOwner().getX() + 3, getOwner().getY() - 3, getOwner().getZ(), (byte) 0);
		spawn(281904, getOwner().getX() - 3, getOwner().getY() + 3, getOwner().getZ(), (byte) 0);
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead() && getOwner().isSpawned()) {
				spawn(281903, getOwner().getX() + 3, getOwner().getY() - 3, getOwner().getZ(), (byte) 0);
				spawn(281904, getOwner().getX() - 3, getOwner().getY() + 3, getOwner().getZ(), (byte) 0);
			}
		}, 60000);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}
}
