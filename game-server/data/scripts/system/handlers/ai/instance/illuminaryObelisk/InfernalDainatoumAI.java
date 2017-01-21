package ai.instance.illuminaryObelisk;

import java.util.Collections;

import com.aionemu.gameserver.ai.AIName;

/**
 * @author Estrayl
 */
@AIName("infernal_dainatoum")
public class InfernalDainatoumAI extends DainatoumAI {

	@Override
	protected int getBombId() {
		return 284860;
	}

	@Override
	protected synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				switch (percent) {
					case 70:
						removeBossEntry();
						break;
					case 50:
					case 10:
						spawnHealers();
						break;
					case 90:
					case 60:
					case 30:
					case 5:
						spawnBombs();
						break;
				}
				percents.remove(percent);
				break;
			}
		}
	}

	@Override
	protected void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 90, 70, 60, 50, 30, 10, 5 });
	}
}
