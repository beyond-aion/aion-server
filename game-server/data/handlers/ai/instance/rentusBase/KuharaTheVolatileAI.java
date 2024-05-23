package ai.instance.rentusBase;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * Still not retail-like and fully refactored.
 * 
 * @author xTz, Estrayl
 */
@AIName("kuhara_the_volatile")
public class KuharaTheVolatileAI extends AggressiveNpcAI {

	private Future<?> activeEventTask, barrelEventTask, bombEventTask;
	private AtomicBoolean isStarted = new AtomicBoolean();
	private boolean canThink = true;

	public KuharaTheVolatileAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isStarted.compareAndSet(false, true)) {
			startActiveEvent();
			startBarrelEvent();
		}
	}

	private void cancelTask(Future<?> task) {
		if (task != null && !task.isCancelled())
			task.cancel(true);
	}

	private void startBarrelEvent() {
		barrelEventTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			switch (Rnd.get(1, 4)) {
				case 1:
					rndSpawn(282394, 126.53f, 274.49f, 209.819f);
					rndSpawn(282394, 126.53f, 274.49f, 209.819f);
					break;
				case 2:
					rndSpawn(282394, 162.22f, 263.89f, 209.819f);
					rndSpawn(282394, 162.22f, 263.89f, 209.819f);
					break;
				case 3:
					rndSpawn(282394, 156.32f, 235.73f, 209.819f);
					rndSpawn(282394, 156.32f, 235.73f, 209.819f);
					break;
				case 4:
					rndSpawn(282394, 119.24f, 245.89f, 209.819f);
					rndSpawn(282394, 119.24f, 245.89f, 209.819f);
					break;
			}
			startBombEvent();
		}, 50000, 50000);
	}

	private void startBombEvent() {
		bombEventTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				PacketSendUtility.broadcastMessage(getOwner(), 1500394);
				canThink = false;
				EmoteManager.emoteStopAttacking(getOwner());
				setStateIfNot(AIState.WALKING);
				SkillEngine.getInstance().getSkill(getOwner(), 19703, 60, getOwner()).useNoAnimationSkill();
				spawnBombEvent();

				bombEventTask = ThreadPoolManager.getInstance().schedule(() -> {
					if (!isDead()) {
						canThink = true;
						Creature creature = getAggroList().getMostHated();
						if (creature == null || creature.isDead() || !getOwner().canSee(creature)) {
							setStateIfNot(AIState.FIGHT);
							think();
						} else {
							getMoveController().abortMove();
							getOwner().setTarget(creature);
							getOwner().getGameStats().renewLastAttackTime();
							getOwner().getGameStats().renewLastAttackedTime();
							getOwner().getGameStats().renewLastChangeTargetTime();
							getOwner().getGameStats().renewLastSkillTime();
							setStateIfNot(AIState.FIGHT);
							handleMoveValidate();
							SkillEngine.getInstance().getSkill(getOwner(), 19375, 60, getOwner()).useNoAnimationSkill();
						}
						deleteNpcs(getPosition().getWorldMapInstance().getNpcs(282394));
						deleteNpcs(getPosition().getWorldMapInstance().getNpcs(282396));
					}
				}, 11000);
			}
		}, 14000);
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null)
				npc.getController().delete();
		}
	}

	private void spawnBombEvent() {
		moveBombToBoss(rndSpawn(282396, 126.53f, 274.49f, 209.819f));
		moveBombToBoss(rndSpawn(282396, 126.53f, 274.49f, 209.819f));
		moveBombToBoss(rndSpawn(282396, 162.22f, 263.89f, 209.819f));
		moveBombToBoss(rndSpawn(282396, 162.22f, 263.89f, 209.819f));
		moveBombToBoss(rndSpawn(282396, 156.32f, 235.73f, 209.819f));
		moveBombToBoss(rndSpawn(282396, 156.32f, 235.73f, 209.819f));
		moveBombToBoss(rndSpawn(282396, 119.24f, 245.89f, 209.819f));
		moveBombToBoss(rndSpawn(282396, 119.24f, 245.89f, 209.819f));
	}

	private void moveBombToBoss(final Npc npc) {
		if (!isDead()) {
			npc.setTarget(getOwner());
			npc.getMoveController().moveToTargetObject();
		}
	}

	private Npc rndSpawn(int npcId, float x, float y, float z) {
		double angleRadians = Math.toRadians(Rnd.nextFloat(360f));
		float distance = Rnd.get(0, 4);
		float x1 = (float) (Math.cos(angleRadians) * distance) + x;
		float y1 = (float) (Math.sin(angleRadians) * distance) + y;
		return (Npc) spawn(npcId, x1, y1, z, (byte) 0);
	}

	private void startActiveEvent() {
		activeEventTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			PacketSendUtility.broadcastMessage(getOwner(), 1500395);
			ThreadPoolManager.getInstance().schedule(() -> {
				if (!isDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), 19704, 60, getOwner()).useNoAnimationSkill();
					ThreadPoolManager.getInstance().schedule(() -> {
						if (!isDead())
							SkillEngine.getInstance().getSkill(getOwner(), 19705, 60, getOwner()).useNoAnimationSkill();
					}, 3500);
				}
			}, 1000);
		}, 8000, 14000);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask(activeEventTask);
		cancelTask(barrelEventTask);
		cancelTask(bombEventTask);
	}

	@Override
	protected void handleBackHome() {
		isStarted.set(false);
		canThink = true;
		cancelTask(activeEventTask);
		cancelTask(barrelEventTask);
		cancelTask(bombEventTask);
		super.handleBackHome();
	}

	@Override
	protected void handleDied() {
		cancelTask(activeEventTask);
		cancelTask(barrelEventTask);
		cancelTask(bombEventTask);
		final WorldPosition p = getPosition();
		if (p != null) {
			deleteNpcs(p.getWorldMapInstance().getNpcs(282394));
			deleteNpcs(p.getWorldMapInstance().getNpcs(282396));
		}
		super.handleDied();
	}

}
