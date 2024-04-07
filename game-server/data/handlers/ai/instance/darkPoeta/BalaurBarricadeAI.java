package ai.instance.darkPoeta;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

import ai.OneDmgNoActionAI;

/**
 * @author Ritsu, Estrayl
 */
@AIName("balaurbarricade")
public class BalaurBarricadeAI extends OneDmgNoActionAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(50, 10);

	public BalaurBarricadeAI(Npc owner) {
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
			case 50 -> spawnProtectors(true);
			case 10 -> spawnProtectors(false);
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

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		hpPhases.reset();
	}
}
