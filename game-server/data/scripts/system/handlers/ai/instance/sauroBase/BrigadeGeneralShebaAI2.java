package ai.instance.sauroBase;

import java.util.List;

import javolution.util.FastTable;
import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("brigade_general_sheba")
public class BrigadeGeneralShebaAI2 extends AggressiveNpcAI2 {

	private List<Integer> percents = FastTable.of(25, 10);

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (percent <= hpPercentage) {
				switch (percent) {
					case 25:
						spawnSpirits(true);
						break;
					case 10:
						spawnSpirits(false);
						break;
				}
				percents.remove(percent);
			}
		}
	}

	private void spawnSpirits(boolean isMid) {
		if (isMid) {
			spawn(284436, 900.12497f, 889.17401f, 412.1f, (byte) 0);
		} else {
			spawn(284436, 913.12497f, 876.17401f, 412.1f, (byte) 45);
			spawn(284436, 900.12497f, 870.17401f, 412.1f, (byte) 30);
			spawn(284436, 886.12497f, 876.17401f, 412.1f, (byte) 16);
			spawn(284436, 881.12497f, 889.17401f, 412.1f, (byte) 0);
			spawn(284436, 899.12497f, 909.17401f, 412.1f, (byte) 90);
			spawn(284436, 913.12497f, 902.17401f, 412.1f, (byte) 78);
			spawn(284436, 918.12497f, 890.17401f, 412.1f, (byte) 61);
		}
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isAlreadyDead())
				AI2Actions.useSkill(BrigadeGeneralShebaAI2.this, 21186);
		}, 7000);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		percents = FastTable.of(25, 10);
	}
}
