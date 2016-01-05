package ai.instance.raksangRuins;

import java.util.Collections;
import java.util.List;

import javolution.util.FastTable;
import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Kill3r
 */
@AIName("diplito")
public class InstructorDiplitoAI2 extends AggressiveNpcAI2 {

	protected List<Integer> percents = new FastTable<>();
	private boolean used = false;

	private void addPercents() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 40 });
	}

	private synchronized void checkhpPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				switch (percent) {
					case 40:
						summonAdd();
						used = true;
						break;
				}
			}
		}
	}

	private void summonAdd() {
		if (!used) {
			spawn(855908, getPosition().getX(), getPosition().getY(), getPosition().getZ(), getPosition().getHeading());
		}
	}

	@Override
	protected void handleSpawned() {
		addPercents();
		super.handleSpawned();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		used = false;
		despawnNpc(getPosition().getWorldMapInstance().getNpc(855908));
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkhpPercentage(getLifeStats().getHpPercentage());
	}

	protected void despawnNpc(Npc npc) {
		if (npc != null)
			npc.getController().onDelete();
	}

}
