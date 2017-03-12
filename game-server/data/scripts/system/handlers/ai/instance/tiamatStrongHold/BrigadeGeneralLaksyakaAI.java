package ai.instance.tiamatStrongHold;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.actions.PlayerActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("brigadegenerallaksyaka")
public class BrigadeGeneralLaksyakaAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> skeletonTask;
	private boolean isFinalBuff;

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (Rnd.chance() < 3)
			spawnSummon();
		if (isHome.compareAndSet(true, false))
			startSkillTask();
		if (!isFinalBuff && getOwner().getLifeStats().getHpPercentage() <= 25) {
			isFinalBuff = true;
			AIActions.useSkill(this, 20731);
		}
	}

	private void startSkillTask() {
		skeletonTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead())
					cancelTask();
				else {
					startSkeletonEvent();
				}
			}
		}, 5000, 40000);
	}

	private void cancelTask() {
		if (skeletonTask != null && !skeletonTask.isCancelled()) {
			skeletonTask.cancel(true);
		}
	}

	private void startSkeletonEvent() {
		Npc tiamatEye = getPosition().getWorldMapInstance().getNpc(283089);// 4.0
		List<Player> players = new ArrayList<>();
		getKnownList().forEachPlayer(player -> {
			if (!PlayerActions.isAlreadyDead(player) && PositionUtil.isInRange(player, tiamatEye, 40)) {
				players.add(player);
			}
		});
		Player player = Rnd.get(players);
		SkillEngine.getInstance().applyEffectDirectly(20865, tiamatEye, player, 30000);
	}

	private void spawnSummon() {
		if (getPosition().getWorldMapInstance().getNpc(283115) == null) {// 4.0
			rndSpawn(283115, 4);// 4.0
		}
	}

	private void rndSpawn(int npcId, int count) {
		for (int i = 0; i < count; i++) {
			SpawnTemplate template = rndSpawnInRange(npcId, 10);
			SpawnEngine.spawnObject(template, getPosition().getInstanceId());
		}
	}

	private SpawnTemplate rndSpawnInRange(int npcId, int dist) {
		float direction = Rnd.get(0, 199) / 100f;
		float x1 = (float) (Math.cos(Math.PI * direction) * dist);
		float y1 = (float) (Math.sin(Math.PI * direction) * dist);
		return SpawnEngine.addNewSingleTimeSpawn(getPosition().getMapId(), npcId, getPosition().getX() + x1, getPosition().getY() + y1, getPosition()
			.getZ(), getPosition().getHeading());
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		getOwner().setNpcType(CreatureType.PEACE);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelTask();
		isFinalBuff = false;
		isHome.set(true);
	}
}
