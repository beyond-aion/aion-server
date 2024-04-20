package ai.instance.tiamatStrongHold;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller, Luzien
 */
@AIName("brigadegeneraltahabata")
public class BrigadeGeneralTahabataAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(96, 75, 60, 55, 40, 25, 20, 10, 7);
	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> piercingStrikeTask;
	private Future<?> fireStormTask;

	public BrigadeGeneralTahabataAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false))
			getPosition().getWorldMapInstance().setDoorState(610, false);
		hpPhases.tryEnterNextPhase(this);
	}

	private void startPiercingStrikeTask() {
		piercingStrikeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (!isDead())
				startPiercingStrikeEvent();
		}, 15000, 20000);
	}

	private void startFireStormTask() {
		fireStormTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (!isDead())
				startFireStormEvent();
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
		ThreadPoolManager.getInstance().schedule(() -> AIActions.useSkill(BrigadeGeneralTahabataAI.this, 20755), 3000);
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

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 96:
				lavaEruptionEvent(283116);// 4.0
				startPiercingStrikeTask();
				break;
			case 75:
				AIActions.useSkill(this, 20761);
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
		ThreadPoolManager.getInstance().schedule(() -> {
			spawn(floor, 679.88f, 1068.88f, 497.88f, (byte) 0);
			spawn(floor + 1, 679.88f, 1068.88f, 497.88f, (byte) 0);
		}, 10000);
	}

	private void rndSpawn(int npcId, int count) {
		for (int i = 0; i < count; i++)
			rndSpawnInRange(npcId, 10);
	}

	private void deleteAdds() {
		getPosition().getWorldMapInstance().getNpcs(283116, 283117, 283118, 283119, 283120, 283121, 283102).forEach(npc -> npc.getController().delete());
	}

	@Override
	protected void handleBackHome() {
		isHome.set(true);
		getPosition().getWorldMapInstance().setDoorState(610, true);
		super.handleBackHome();
		hpPhases.reset();
		getEffectController().removeEffect(20942);
		deleteAdds();
		cancelPiercingStrike();
		cancelFireStorm();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelPiercingStrike();
		cancelFireStorm();
	}

	@Override
	protected void handleDied() {
		getPosition().getWorldMapInstance().setDoorState(610, true);
		super.handleDied();
		deleteAdds();
		cancelPiercingStrike();
		cancelFireStorm();
	}

}
