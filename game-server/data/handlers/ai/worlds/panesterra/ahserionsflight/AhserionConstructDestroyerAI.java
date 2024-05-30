package ai.worlds.panesterra.ahserionsflight;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * Should also be able to request support once by dropping below 35% HP.
 *
 * @author Estrayl
 */
@AIName("ahserion_construct_destroyer")
public class AhserionConstructDestroyerAI extends AhserionAggressiveNpcAI {

	private final AtomicBoolean isActivated = new AtomicBoolean();

	public AhserionConstructDestroyerAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getSpawnTemplate().getHandlerType() == SpawnHandlerType.ATTACKER) {
			getOwner().getController().addTask(TaskId.DESPAWN,
				ThreadPoolManager.getInstance().schedule(() -> getOwner().getController().deleteIfAliveOrCancelRespawn(), 9, TimeUnit.MINUTES));
		}
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (isActivated.compareAndSet(false, true)) {
			WorldPosition p = getPosition();
			spawn(297191, p.getX() + 5, p.getY() - 5, p.getZ() + 0.5f, (byte) 0); // Ahserion Troopers Assassin
			spawn(297191, p.getX() - 5, p.getY() + 5, p.getZ() + 0.5f, (byte) 0); // Ahserion Troopers Assassin
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 17446 && skillLevel == 57)
			addHateToRndTarget();
	}

	private void despawnAssassins() {
		getKnownList().forEachNpc(npc -> {
			if (npc.getNpcId() == 297191)
				npc.getController().deleteIfAliveOrCancelRespawn();
		});
	}

	@Override
	protected void handleBackHome() {
		despawnAssassins();
		isActivated.set(false);
		super.handleBackHome();
	}

	@Override
	protected void handleDied() {
		despawnAssassins();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		despawnAssassins();
		super.handleDespawned();
	}
}
