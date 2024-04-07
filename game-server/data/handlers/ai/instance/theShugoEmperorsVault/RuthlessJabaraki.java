package ai.instance.theShugoEmperorsVault;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author Yeats
 */
@AIName("ruthless_jabaraki")
public class RuthlessJabaraki extends IDSweep_Bosses implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(96, 80, 60, 45, 40, 35);
	private List<Npc> spawnedAdds = new ArrayList<>();

	public RuthlessJabaraki(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 95 -> spawnAdds(1);
			case 80 -> spawnAdds(1);
			case 65 -> spawnAdds(2);
			case 50 -> spawnAdds(3);
			case 35 -> spawnAdds(4);
			case 20 -> spawnAdds(5);
		}
	}

	private void spawnAdds(int stage) {
		switch (stage) {
			case 1:
				spawnedAdds.add((Npc) spawn(235631, 541.625f, 407.0712f, 395.46875f, (byte) 110));
				spawnedAdds.add((Npc) spawn(235631, 538.86127f, 389.05258f, 395.4342f, (byte) 8));
				spawnedAdds.add((Npc) spawn(235631, 558.6854f, 382.02362f, 395.16785f, (byte) 43));
				spawnedAdds.add((Npc) spawn(235631, 564.9021f, 400.29718f, 395.46262f, (byte) 69));
				break;
			case 2:
				spawnedAdds.add((Npc) spawn(235630, 541.625f, 407.0712f, 395.46875f, (byte) 110));
				spawnedAdds.add((Npc) spawn(235630, 538.86127f, 389.05258f, 395.4342f, (byte) 8));
				spawnedAdds.add((Npc) spawn(235630, 558.6854f, 382.02362f, 395.16785f, (byte) 43));
				spawnedAdds.add((Npc) spawn(235630, 564.9021f, 400.29718f, 395.46262f, (byte) 69));

				spawnedAdds.add((Npc) spawn(235630, 540.625f, 406.0712f, 395.76875f, (byte) 110));
				spawnedAdds.add((Npc) spawn(235630, 539.86127f, 390.05258f, 395.7342f, (byte) 8));
				spawnedAdds.add((Npc) spawn(235630, 559.6854f, 383.02362f, 395.46785f, (byte) 43));
				spawnedAdds.add((Npc) spawn(235630, 565.9021f, 401.29718f, 395.76262f, (byte) 69));
				break;
			case 3:
				spawnedAdds.add((Npc) spawn(235629, 541.625f, 407.0712f, 395.46875f, (byte) 110));
				spawnedAdds.add((Npc) spawn(235629, 538.86127f, 389.05258f, 395.4342f, (byte) 8));
				spawnedAdds.add((Npc) spawn(235629, 558.6854f, 382.02362f, 395.16785f, (byte) 43));
				spawnedAdds.add((Npc) spawn(235629, 564.9021f, 400.29718f, 395.46262f, (byte) 69));

				spawnedAdds.add((Npc) spawn(235629, 540.625f, 406.0712f, 395.76875f, (byte) 110));
				spawnedAdds.add((Npc) spawn(235629, 539.86127f, 390.05258f, 395.7342f, (byte) 8));
				spawnedAdds.add((Npc) spawn(235629, 559.6854f, 383.02362f, 395.46785f, (byte) 43));
				spawnedAdds.add((Npc) spawn(235629, 565.9021f, 401.29718f, 395.76262f, (byte) 69));
				break;
			case 4:
				spawnedAdds.add((Npc) spawn(235630, 541.625f, 407.0712f, 395.46875f, (byte) 110));
				spawnedAdds.add((Npc) spawn(235630, 538.86127f, 389.05258f, 395.4342f, (byte) 8));
				spawnedAdds.add((Npc) spawn(235630, 558.6854f, 382.02362f, 395.16785f, (byte) 43));
				spawnedAdds.add((Npc) spawn(235630, 564.9021f, 400.29718f, 395.46262f, (byte) 69));

				spawnedAdds.add((Npc) spawn(235630, 540.625f, 406.0712f, 395.76875f, (byte) 110));
				spawnedAdds.add((Npc) spawn(235630, 539.86127f, 390.05258f, 395.7342f, (byte) 8));
				spawnedAdds.add((Npc) spawn(235630, 559.6854f, 383.02362f, 395.46785f, (byte) 43));
				spawnedAdds.add((Npc) spawn(235630, 565.9021f, 401.29718f, 395.76262f, (byte) 69));

				spawnedAdds.add((Npc) spawn(235630, 541.625f, 407.0712f, 396.06875f, (byte) 110));
				spawnedAdds.add((Npc) spawn(235630, 540.86127f, 391.05258f, 396.0342f, (byte) 8));
				spawnedAdds.add((Npc) spawn(235630, 560.6854f, 384.02362f, 395.86785f, (byte) 43));
				spawnedAdds.add((Npc) spawn(235630, 566.9021f, 402.29718f, 395.86262f, (byte) 69));
				break;
			case 5:
				spawnedAdds.add((Npc) spawn(235631, 541.625f, 407.0712f, 395.46875f, (byte) 110));
				spawnedAdds.add((Npc) spawn(235631, 538.86127f, 389.05258f, 395.4342f, (byte) 8));
				spawnedAdds.add((Npc) spawn(235631, 558.6854f, 382.02362f, 395.16785f, (byte) 43));
				spawnedAdds.add((Npc) spawn(235631, 564.9021f, 400.29718f, 395.46262f, (byte) 69));

				spawnedAdds.add((Npc) spawn(235631, 540.625f, 406.0712f, 395.76875f, (byte) 110));
				spawnedAdds.add((Npc) spawn(235631, 539.86127f, 390.05258f, 395.7342f, (byte) 8));
				spawnedAdds.add((Npc) spawn(235631, 559.6854f, 383.02362f, 395.46785f, (byte) 43));
				spawnedAdds.add((Npc) spawn(235631, 565.9021f, 401.29718f, 395.76262f, (byte) 69));

				spawnedAdds.add((Npc) spawn(235631, 541.625f, 407.0712f, 396.06875f, (byte) 110));
				spawnedAdds.add((Npc) spawn(235631, 540.86127f, 391.05258f, 396.0342f, (byte) 8));
				spawnedAdds.add((Npc) spawn(235631, 560.6854f, 384.02362f, 395.86785f, (byte) 43));
				spawnedAdds.add((Npc) spawn(235631, 566.9021f, 402.29718f, 395.86262f, (byte) 69));
				break;
		}
		startHate();
	}

	private void startHate() {
		for (Npc npc : spawnedAdds) {
			if (npc != null && !npc.isDead()) {
				npc.getAggroList().addHate(getOwner().getAggroList().getMostHated(), 1);
			}
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		getOwner().getController().delete();
	}
}
