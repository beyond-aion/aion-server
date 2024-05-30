package ai.worlds.panesterra.ahserionsflight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.templates.spawns.panesterra.AhserionsFlightSpawnTemplate;

import ai.AggressiveNoLootNpcAI;

import com.aionemu.gameserver.utils.ThreadPoolManager;

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
		List<VisibleObject> objects = getKnownList().getKnownObjects().values().stream()
			.filter(o -> o instanceof Creature creature && !creature.isDead() && getOwner().isEnemy(creature))
			.collect(Collectors.toCollection(ArrayList::new));
		Collections.shuffle(objects);
		Creature rndTarget = (Creature) Rnd.get(objects);
		if (rndTarget != null)
			getAggroList().addHate(rndTarget, 100000);
	}

	@Override
	protected AhserionsFlightSpawnTemplate getSpawnTemplate() {
		return (AhserionsFlightSpawnTemplate) super.getSpawnTemplate();
	}
}
