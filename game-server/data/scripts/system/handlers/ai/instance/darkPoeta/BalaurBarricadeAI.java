package ai.instance.darkPoeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;

import ai.OneDmgNoActionAI;

/**
 * @author Ritsu
 * @modified Estrayl 12.06.2017
 */
@AIName("balaurbarricade")
public class BalaurBarricadeAI extends OneDmgNoActionAI {

	protected List<Integer> percents = new ArrayList<>();

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 50:
						spawnProtectors(true);
						break;
					case 10:
						spawnProtectors(false);
						break;
				}
				break;
			}
		}
	}
	
	private void spawnProtectors(boolean isFirstSpawn) {
		switch (getNpcId()) {
			case 700517:
				spawn(isFirstSpawn ? 215262 : 214883, 282.2922f, 1003.0374f, 113.1999f, (byte) 25);
				spawn(isFirstSpawn ? 215262 : 215263, 289.5031f, 1000.1637f, 112.9796f, (byte) 25);
				break;
			case 700556:
				spawn(isFirstSpawn ? 215262 : 214883, 315.8379f, 982.8948f, 111.0691f, (byte) 17);
				spawn(isFirstSpawn ? 215262 : 215263, 309.0993f, 989.5142f, 112.6760f, (byte) 17);
				break;
			case 700558:
				spawn(isFirstSpawn ? 215262 : 214883, 199.7505f, 843.6876f, 100.6562f, (byte) 59);
				spawn(isFirstSpawn ? 215262 : 215263, 201.9819f, 853.4918f, 101.0603f, (byte) 59);
				break;
		}
	}	

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 50, 10 });
	}

	@Override
	protected void handleSpawned() {
		addPercent();
		super.handleSpawned();
	}

	@Override
	protected void handleBackHome() {
		addPercent();
		super.handleBackHome();
	}
}
