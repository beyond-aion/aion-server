package ai.instance.tiamatStrongHold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 * @modified Luzien
 */
@AIName("brigadegeneraltahabata")
public class BrigadeGeneralTahabataAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private AtomicBoolean isEndPiercingStrike = new AtomicBoolean(true);
	private Future<?> piercingStrikeTask;
	private AtomicBoolean isEndFireStorm = new AtomicBoolean(true);
	private Future<?> fireStormTask;
	protected List<Integer> percents = new ArrayList<>();

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false) && getPosition().getWorldMapInstance().getDoors().get(610) != null) {
			getPosition().getWorldMapInstance().getDoors().get(610).setOpen(false);
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void startPiercingStrikeTask() {
		piercingStrikeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead())
					cancelPiercingStrike();
				else {
					startPiercingStrikeEvent();
				}
			}

		}, 15000, 20000);
	}

	private void startFireStormTask() {
		fireStormTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead())
					cancelFireStorm();
				else {
					startFireStormEvent();
				}
			}

		}, 10000, 20000);
	}

	private void startFireStormEvent() {
		if (getPosition().getWorldMapInstance().getNpc(283045) == null) {
			AIActions.useSkill(this, 20758);
			rndSpawn(283045, Rnd.get(1, 4));
		}
	}

	private void startPiercingStrikeEvent() {
		teleportRandomPlayer();
		Skill skill = SkillEngine.getInstance().getSkill(getOwner(), 20754, 60, getOwner());
		if (skill != null) {
			skill.useNoAnimationSkill();
			if (!skill.getEffectedList().isEmpty())
				useMultiSkill();
		}
	}

	private void useMultiSkill() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.useSkill(BrigadeGeneralTahabataAI.this, 20755);
			}

		}, 3000);
	}

	private void teleportRandomPlayer() {
		List<Player> players = new ArrayList<>();
		getKnownList().forEachPlayer(player -> {
			if (!player.isDead() && PositionUtil.isInRange(player, getOwner(), 40)) {
				players.add(player);
			}
		});
		Player target = Rnd.get(players);
		if (target == null)
			return;
		AIActions.targetCreature(this, target);
		World.getInstance().updatePosition(getOwner(), target.getX(), target.getY(), target.getZ(), (byte) 0);
		PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
	}

	private void cancelPiercingStrike() {
		if (piercingStrikeTask != null && !piercingStrikeTask.isCancelled()) {
			piercingStrikeTask.cancel(true);
		}
	}

	private void cancelFireStorm() {
		if (fireStormTask != null && !fireStormTask.isCancelled()) {
			fireStormTask.cancel(true);
		}
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 96:
						lavaEruptionEvent(283116);// 4.0
						if (isEndPiercingStrike.compareAndSet(true, false))
							startPiercingStrikeTask();
						break;
					case 75:
						AIActions.useSkill(this, 20761);
						if (isEndFireStorm.compareAndSet(true, false))
							startFireStormTask();
						break;
					case 60:
						AIActions.useSkill(this, 20761);
						break;
					case 55:
						lavaEruptionEvent(283118);// 4.0
						break;
					case 40:
					case 25:
						AIActions.useSkill(this, 20761);
						break;
					case 20:
						cancelFireStorm();
						cancelPiercingStrike();
						lavaEruptionEvent(283120);// 4.0
						break;
					case 10:
						AIActions.useSkill(this, 20942);
						break;
					case 7:
						AIActions.useSkill(this, 20883);
						spawn(283102, 679.88f, 1068.88f, 497.88f, (byte) 0);// 4.0
						break;
				}
				break;
			}
		}
	}

	private void lavaEruptionEvent(final int floorId) {
		AIActions.targetSelf(BrigadeGeneralTahabataAI.this);
		AIActions.useSkill(BrigadeGeneralTahabataAI.this, 20756);
		if (getPosition().getWorldMapInstance().getNpc(283051) == null) {// 4.0
			spawn(283051, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);// 4.0
		}
		spawnfloor(floorId);
	}

	private void spawnfloor(final int floor) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				spawn(floor, 679.88f, 1068.88f, 497.88f, (byte) 0);
				spawn(floor + 1, 679.88f, 1068.88f, 497.88f, (byte) 0);
			}

		}, 10000);
	}

	private void rndSpawn(int npcId, int count) {
		for (int i = 0; i < count; i++) {
			WorldMapInstance instance = getPosition().getWorldMapInstance();
			if (instance != null) {
				SpawnTemplate template = rndSpawnInRange(npcId, 10);
				SpawnEngine.spawnObject(template, getPosition().getInstanceId());
			}
		}
	}

	private SpawnTemplate rndSpawnInRange(int npcId, int dist) {
		float direction = Rnd.get(0, 199) / 100f;
		float x1 = (float) (Math.cos(Math.PI * direction) * dist);
		float y1 = (float) (Math.sin(Math.PI * direction) * dist);
		return SpawnEngine.newSingleTimeSpawn(getPosition().getMapId(), npcId, getPosition().getX() + x1, getPosition().getY() + y1, getPosition()
			.getZ(), getPosition().getHeading());
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().delete();
			}
		}
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 96, 75, 60, 55, 40, 25, 20, 10, 7 });
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}

	private void deleteAdds() {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		deleteNpcs(instance.getNpcs(283116));// 4.0
		deleteNpcs(instance.getNpcs(283117));// 4.0
		deleteNpcs(instance.getNpcs(283118));// 4.0
		deleteNpcs(instance.getNpcs(283119));// 4.0
		deleteNpcs(instance.getNpcs(283120));// 4.0
		deleteNpcs(instance.getNpcs(283121));// 4.0
		deleteNpcs(instance.getNpcs(283102));// 4.0
	}

	@Override
	protected void handleBackHome() {
		addPercent();
		isHome.set(true);
		getPosition().getWorldMapInstance().getDoors().get(610).setOpen(true);
		super.handleBackHome();
		getEffectController().removeEffect(20942);
		deleteAdds();
		cancelPiercingStrike();
		cancelFireStorm();
		isEndPiercingStrike.set(true);
		isEndFireStorm.set(true);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelPiercingStrike();
		cancelFireStorm();
	}

	@Override
	protected void handleDied() {
		percents.clear();
		getPosition().getWorldMapInstance().getDoors().get(610).setOpen(true);
		super.handleDied();
		deleteAdds();
		cancelPiercingStrike();
		cancelFireStorm();
	}

}
