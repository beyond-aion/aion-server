package ai.instance.raksang;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("illusion_maseter_sharik")
public class IllusionMasterSharikAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(80);
	private AtomicBoolean startedEvent = new AtomicBoolean(false);
	private int position = 1;
	private int percent = 100;
	private Future<?> phaseTask;

	public IllusionMasterSharikAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		WorldPosition p = getPosition();
		if (p.getX() == 738.065f && p.getY() == 311.606f) {
			position = 1;
		} else {
			position = 2;
		}
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player player && PositionUtil.isInRange(getOwner(), player, 30)) {
			if (startedEvent.compareAndSet(false, true)) {
				PacketSendUtility.broadcastMessage(getOwner(), 1401112);
			}
		}
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		PacketSendUtility.broadcastToMap(getOwner(), 1401136);
		if (position == 1) {
			spawn(730446, 738.766f, 317.482f, 911.897f, (byte) 0, 5);
		} else {
			spawn(730447, 735.909f, 265.696f, 911.897f, (byte) 0, 278);
		}
		startPhaseTask();
	}

	private void startPhaseTask() {
		phaseTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead()) {
					cancelPhaseTask();
				} else {
					PacketSendUtility.broadcastMessage(getOwner(), 1401114);
					SkillEngine.getInstance().getSkill(getOwner(), 19981, 46, getOwner()).useNoAnimationSkill();
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (!isDead()) {
								SkillEngine.getInstance().getSkill(getOwner(), 19901, 44, getOwner()).useNoAnimationSkill();
								if (percent <= 50) {
									ThreadPoolManager.getInstance().schedule(new Runnable() {

										@Override
										public void run() {
											if (!isDead()) {
												SkillEngine.getInstance().getSkill(getOwner(), 19903, 44, getOwner()).useNoAnimationSkill();
											}
										}

									}, 13000);
								}
							}
						}

					}, 3000);
				}
			}

		}, 3000, 40000);
	}

	private void cancelPhaseTask() {
		if (phaseTask != null && !phaseTask.isDone()) {
			phaseTask.cancel(true);
		}
	}

	@Override
	protected void handleBackHome() {
		despawnMirrors();
		cancelPhaseTask();
		super.handleBackHome();
		PacketSendUtility.broadcastToMap(getOwner(), 1401137);
		if (position == 1) {
			spawn(217425, 736.21704f, 270.8546f, 910.678f, (byte) 53);
		} else {
			spawn(217425, 738.065f, 311.606f, 910.678f, (byte) 53);
		}
		AIActions.deleteOwner(this);
	}

	private void despawnMirrors() {
		WorldPosition p = getPosition();
		if (p != null) {
			WorldMapInstance instance = p.getWorldMapInstance();
			if (instance != null) {
				deleteNpcs(instance.getNpcs(730446));
				deleteNpcs(instance.getNpcs(730447));
			}
		}
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().delete();
			}
		}
	}

	@Override
	protected void handleDespawned() {
		cancelPhaseTask();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		despawnMirrors();
		cancelPhaseTask();
		getPosition().getWorldMapInstance().setDoorState(294, true);
		getPosition().getWorldMapInstance().setDoorState(295, true);
		super.handleDied();
	}

}
