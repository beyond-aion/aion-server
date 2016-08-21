package ai.instance.dragonLordsRefuge;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.PlayerActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI2;
import javolution.util.FastTable;

/**
 * @author Cheatkiller
 */
@AIName("calindiflamelord60")
public class CalindiFlamelordAI2 extends AggressiveNpcAI2 {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> trapTask;
	private boolean isFinalBuff;

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false))
			startSkillTask();
		if (!isFinalBuff) {
			blazeEngraving();
			if (getOwner().getLifeStats().getHpPercentage() <= 12) {
				isFinalBuff = true;
				cancelTask();
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(20915, 1, 100, true)));
			}
		}
	}

	private void startSkillTask() {
		trapTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (isAlreadyDead())
				cancelTask();
			else {
				startHallucinatoryVictoryEvent();
			}
		}, 5000, 80000);
	}

	private void cancelTask() {
		if (trapTask != null && !trapTask.isCancelled()) {
			trapTask.cancel(true);
		}
	}

	private void startHallucinatoryVictoryEvent() {
		if (getPosition().getWorldMapInstance().getNpc(730695) == null && getPosition().getWorldMapInstance().getNpc(730696) == null) {
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(20911, 1, 100, true)));
		}
	}

	@Override
	public void fireOnEndCastEvents(NpcSkillEntry usedSkill) {
		switch (usedSkill.getSkillId()) {
			case 20911:
				SkillEngine.getInstance().applyEffectDirectly(20590, getOwner(), getOwner(), 0);
				SkillEngine.getInstance().applyEffectDirectly(20591, getOwner(), getOwner(), 0);
				spawn(730695, 482.21f, 458.06f, 427.42f, (byte) 98);
				spawn(730696, 482.21f, 571.16f, 427.42f, (byte) 22);
				rndSpawn();
				break;
			case 20913:
				Player target = getRandomTarget();
				if (target != null) {
					spawn(283130, target.getX(), target.getY(), target.getZ(), (byte) 0);
				}
		}
	}

	private void blazeEngraving() {
		if (Rnd.get(0, 100) < 2 && getPosition().getWorldMapInstance().getNpc(283130) == null) {
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(20913, 60, 100, true)));
		}
	}

	private void rndSpawn() {
		for (int i = 0; i < 10; i++) {
			SpawnTemplate template = rndSpawnInRange();
			SpawnEngine.spawnObject(template, getPosition().getInstanceId());
		}
	}

	private SpawnTemplate rndSpawnInRange() {
		float direction = Rnd.get(0, 199) / 100f;
		int range = Rnd.get(5, 20);
		float x1 = (float) (Math.cos(Math.PI * direction) * range);
		float y1 = (float) (Math.sin(Math.PI * direction) * range);
		return SpawnEngine.addNewSingleTimeSpawn(getPosition().getMapId(), 283132, getPosition().getX() + x1, getPosition().getY() + y1, getPosition()
			.getZ(), getPosition().getHeading());
	}

	private Player getRandomTarget() {
		List<Player> players = getKnownList().getKnownPlayers().values().stream().filter(player -> !PlayerActions.isAlreadyDead(player) && MathUtil.isIn3dRange(player, getOwner(), 50)).collect(Collectors.toCollection(FastTable::new));
		if (players.isEmpty())
			return null;
		return players.get(Rnd.get(players.size()));
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
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
