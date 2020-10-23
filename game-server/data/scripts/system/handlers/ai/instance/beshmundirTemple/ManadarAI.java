package ai.instance.beshmundirTemple;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("manadar")
public class ManadarAI extends AggressiveNpcAI {

	private boolean isStart = false;

	public ManadarAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 90 && !isStart) {
			isStart = true;
			check();
		}
	}

	@Override
	protected void handleBackHome() {
		isStart = false;
		super.handleBackHome();
	}

	private void check() {
		if (getPosition().isSpawned() && !isDead() && isStart) {
			for (int i = 0; i < 5; i++) {
				rndSpawnInRange(Rnd.nextBoolean() ? 281545 : 281756, 4, 11);
			}
			ThreadPoolManager.getInstance().schedule(this::check, 6000);
		}
	}
}
