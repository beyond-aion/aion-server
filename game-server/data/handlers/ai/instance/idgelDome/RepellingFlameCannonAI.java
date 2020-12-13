package ai.instance.idgelDome;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu, Estrayl
 */
@AIName("repelling_flame_cannon")
public class RepellingFlameCannonAI extends NpcAI {

	private Future<?> skillTask;

	public RepellingFlameCannonAI(Npc owner) {
		super(owner);
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 0;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> AIActions.useSkill(this, 21648), 1000, 1000);
	}

	@Override
	protected void handleDespawned() {
		skillTask.cancel(true);
		super.handleDespawned();
	}
}
