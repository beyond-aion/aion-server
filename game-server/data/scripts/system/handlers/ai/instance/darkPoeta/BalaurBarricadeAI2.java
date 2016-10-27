package ai.instance.darkPoeta;

import java.util.Collections;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;

import ai.AggressiveNpcAI2;
import javolution.util.FastTable;

/**
 * @author Ritsu
 */

@AIName("balaurbarricade")
public class BalaurBarricadeAI2 extends AggressiveNpcAI2 {

	protected List<Integer> percents = new FastTable<>();

	@Override
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
		return 1;
	}

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
					case 60:
					case 10:
						sp();
						break;
				}

				break;
			}
		}
	}

	private void sp() {
		Npc npc = getOwner();
		float direction = Rnd.get(0, 199) / 100f;
		int distance = Rnd.get(1, 4);
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		if (npc.getNpcId() == 700517 || npc.getNpcId() == 700556) {
			spawn(215262, npc.getX() + x1, npc.getY() + y1, npc.getZ(), (byte) 0);
			spawn(215262, npc.getX() + y1, npc.getY() + x1, npc.getZ(), (byte) 0);
		} else if (npc.getNpcId() == 700558) {
			spawn(215262, npc.getX() + x1, npc.getY() + y1, npc.getZ(), (byte) 0);
			spawn(214883, npc.getX() + y1, npc.getY() + x1, npc.getZ(), (byte) 0);
		}
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 60, 10 });
	}

	@Override
	protected void handleSpawned() {
		addPercent();
		super.handleDespawned();
	}

	@Override
	protected void handleBackHome() {
		addPercent();
		super.handleBackHome();
	}

	@Override
	protected void handleDespawned() {
		percents.clear();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		percents.clear();
		super.handleDied();
	}
}
