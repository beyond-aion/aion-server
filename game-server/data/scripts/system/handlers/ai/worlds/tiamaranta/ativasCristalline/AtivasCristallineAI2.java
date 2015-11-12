package ai.worlds.tiamaranta.ativasCristalline;

import java.util.Collections;
import java.util.List;

import javolution.util.FastTable;
import ai.AggressiveNpcAI2;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author Ritsu
 */

@AIName("ativascristalline")
public class AtivasCristallineAI2 extends AggressiveNpcAI2 {
	
	private List<Integer> percents = new FastTable<>();

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	@Override
	protected void handleBackHome() {
		addPercent();
		super.handleBackHome();
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				switch (percent) {
					case 90:
					case 30:
						topazKomad();
						break;
					case 60:
					case 10:
						garnetKomad();
						break;
				}
			}
			percents.remove(percent);
			break;
		}
	}
	
	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 100, 75, 50, 25 });
	}

	private void garnetKomad() {
		if (getPosition().isSpawned() && !isAlreadyDead()) {
			for (int i = 0; i < 1; i++) {
				int distance = Rnd.get(3, 5);
				int nrNpc = Rnd.get(1, 0);
				switch (nrNpc) {
					case 1:
						nrNpc = 282708; // Garnet Komad.
						break;
				}
				rndSpawnInRange(nrNpc, distance);
			}
		}
	}

	private void topazKomad() {
		if (getPosition().isSpawned() && !isAlreadyDead()) {
			for (int i = 0; i < 1; i++) {
				int distance = Rnd.get(3, 5);
				int nrNpc = Rnd.get(1, 0);
				switch (nrNpc) {
					case 1:
						nrNpc = 282709; // Topaz Komad.
						break;
				}
				rndSpawnInRange(nrNpc, distance);
			}
		}
	}

	private void rndSpawnInRange(int npcId, float distance) {
		float direction = Rnd.get(0, 199) / 100f;
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		spawn(npcId, getPosition().getX() + x1, getPosition().getY() + y1, getPosition().getZ(), (byte) 0);
	}
}
