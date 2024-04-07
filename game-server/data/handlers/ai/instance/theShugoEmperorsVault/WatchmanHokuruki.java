package ai.instance.theShugoEmperorsVault;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Yeats
 */
@AIName("watchman_hokuruki")
public class WatchmanHokuruki extends IDSweep_Bosses implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(100, 75, 50, 25, 15);
	private final List<Integer> randomPositions = Collections.synchronizedList(Arrays.asList(1, 2, 3));

	public WatchmanHokuruki(Npc owner) {
		super(owner);
		Collections.shuffle(randomPositions);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 100, 15 -> rndSpawn(235632, 4);
			case 75 -> spawnAdds(randomPositions.get(0));
			case 50 -> spawnAdds(randomPositions.get(1));
			case 25 -> spawnAdds(randomPositions.get(2));
		}
	}

	private void spawnAdds(int position) {
		switch (position) {
			case 1:
				spawn(236083, 472.8545f, 644.94f, 395.50546f, (byte) 113);
				spawn(235649, 473.7f, 648.9f, 395.55615f, (byte) 116);
				spawn(235649, 471.75f, 641f, 395.27322f, (byte) 116);
				break;
			case 2:
				spawn(236083, 490f, 655.1424f, 394.69073f, (byte) 84);
				spawn(235649, 485.812f, 656.14f, 395.375f, (byte) 90);
				spawn(235649, 493.59f, 652.62f, 395.41345f, (byte) 77);
				break;
			case 3:
				spawn(236083, 493f, 629.34f, 394.89963f, (byte) 41);
				spawn(235649, 496.56f, 631.78f, 395.20126f, (byte) 49);
				spawn(235649, 488.86f, 627.54f, 395.27853f, (byte) 38);
				break;
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		hpPhases.reset();
		Collections.shuffle(randomPositions);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		getOwner().getController().delete();
	}

	private void rndSpawn(int npcId, int count) {
		for (int i = 0; i < count; i++) {
			Npc npc = (Npc) rndSpawnInRange(npcId, 3, 5);
			npc.getAggroList().addHate(getOwner().getAggroList().getMostHated(), 1);
		}
	}
}
