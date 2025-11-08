package ai.worlds.panesterra.ahserionsflight;

import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.attack.AggroTarget;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Yeats
 */
@AIName("ahserion_aggressive_npc")
public class AhserionAggressiveNpcAI extends AggressiveNoLootNpcAI {

	public AhserionAggressiveNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getNpcId() == 277242) {
			getOwner().getController().addTask(TaskId.DESPAWN,
				ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().deleteIfAliveOrCancelRespawn(), 8, TimeUnit.MINUTES));
		}
	}

	protected void addHateToRndTarget() {
		getAggroList().addHate(getAggroList().getTarget(AggroTarget.RANDOM), 100000);
	}

	@Override
	protected AhserionsFlightSpawnTemplate getSpawnTemplate() {
		return (AhserionsFlightSpawnTemplate) super.getSpawnTemplate();
	}
}
