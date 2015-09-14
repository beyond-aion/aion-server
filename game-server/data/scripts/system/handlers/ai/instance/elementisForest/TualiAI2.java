package ai.instance.elementisForest;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Luzien, xTz
 */
@AIName("tuali")
public class TualiAI2 extends AggressiveNpcAI2 {

	private AtomicBoolean isStart = new AtomicBoolean(false);
	private AtomicBoolean isStart65Event = new AtomicBoolean(false);
	private AtomicBoolean isStart45Event = new AtomicBoolean(false);
	private AtomicBoolean isStart25Event = new AtomicBoolean(false);
	private Future<?> task;

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isStart.compareAndSet(false, true)) {
			NpcShoutsService.getInstance().sendMsg(getOwner(), 1500454, getObjectId(), true, 0, 0);
			scheduleSkills();

			task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					if (!isAlreadyDead()) {
						SkillEngine.getInstance().getSkill(getOwner(), 19348, 60, getOwner()).useNoAnimationSkill();
						int size = getPosition().getWorldMapInstance().getNpcs(282308).size();
						for (int i = 0; i < 6; i++) {
							if (size >= 12) {
								break;
							}
							size++;
							rndSpawn(282307);
						}
						NpcShoutsService.getInstance().sendMsg(getOwner(), 1401378, 6000);
					}
				}

			}, 20000, 50000);
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage <= 65) {
			if (isStart65Event.compareAndSet(false, true)) {
				buff();
			}
		}
		if (hpPercentage <= 45) {
			if (isStart45Event.compareAndSet(false, true)) {
				buff();
			}
		}
		if (hpPercentage <= 25) {
			if (isStart25Event.compareAndSet(false, true)) {
				buff();
			}
		}
	}

	private void buff() {
		SkillEngine.getInstance().getSkill(getOwner(), 19511, 60, getOwner()).useNoAnimationSkill();
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1500456, getObjectId(), true, 0, 0);
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1401041, 3500);
	}

	private void rndSpawn(int npcId) {
		float direction = Rnd.get(0, 199) / 100f;
		int distance = Rnd.get(5, 12);
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		WorldPosition p = getPosition();
		spawn(npcId, p.getX() + x1, p.getY() + y1, p.getZ(), (byte) 0);
	}

	@Override
	public void handleDied() {
		cancelTask();
		super.handleDied();
		NpcShoutsService.getInstance().sendMsg(getOwner(), 1500457, getObjectId(), true, 0, 0);
	}

	private void cancelTask() {
		if (task != null && !task.isDone()) {
			task.cancel(true);
		}
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	public void handleBackHome() {
		cancelTask();
		WorldPosition p = getPosition();
		if (p != null) {
			WorldMapInstance instance = p.getWorldMapInstance();
			if (instance != null) {
				deleteNpcs(instance.getNpcs(282307));
				deleteNpcs(instance.getNpcs(282308));
			}
		}
		super.handleBackHome();
		isStart65Event.set(false);
		isStart45Event.set(false);
		isStart25Event.set(false);
		isStart.set(false);
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().onDelete();
			}
		}
	}

	private void scheduleSkills() {
		if (isAlreadyDead() || !isStart.get()) {
			return;
		}
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.useSkill(TualiAI2.this, 19512 + Rnd.get(5));
				scheduleSkills();
			}
		}, Rnd.get(18, 22) * 1000);
	}
}
