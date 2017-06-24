package ai.instance.danuarReliquary;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 * @reworked Luzien, Yeats 26.05.2016
 */
@AIName("enraged_queen_modor")
public class EnragedQueenModorAI extends AggressiveNpcAI {

	private Future<?> skillTask;
	private AtomicBoolean isHome = new AtomicBoolean(true);
	private boolean up;
	private int stage = 0;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (isHome.compareAndSet(true, false)) {
			ThreadPoolManager.getInstance().schedule(() -> {
				PacketSendUtility.broadcastMessage(getOwner(), 1500743);
				onSpawnSkills();
			}, 1000);
		}
	}

	private void startStage(int stage) {
		if (!canAct())
			return;
		switch (stage) {
			case 1:
				PacketSendUtility.broadcastMessage(getOwner(), 1500750);
				rendSpace(true);
				spawn(284659, 256.57727f, 278.18225f, 241.54623f, (byte) 90);
				spawn(284660, 246.65663f, 275.51996f, 241.54623f, (byte) 96);
				spawn(284660, 246.65663f, 275.51996f, 241.54623f, (byte) 96);
				spawn(284661, 266.26517f, 273.97614f, 241.54623f, (byte) 83);
				startIceTask();
				ThreadPoolManager.getInstance().schedule(() -> {
					if (canAct()) {
						startStage(2);
					}
				}, 70000);
				break;
			case 2:
				rendSpace(false);
				startGroundTask();
				ThreadPoolManager.getInstance().schedule(() -> {
					if (canAct()) {
						cancelSkillTask();
						startStage(3);
					}

				}, 70000);
				break;
			case 3:
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21170, 10, 100, true)));
				break;

		}
	}

	private void rendSpace(boolean up) {
		this.up = up;
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21165, 60, 100, true)));
	}

	private void onSpawnSkills() {
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21171, 60, 100, true)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21169, 60, 100, true)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21181, 60, 100, true)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21174, 60, 100, true)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21175, 60, 100, true)));
		ThreadPoolManager.getInstance().schedule(() -> {
			if (canAct()) {
				startStage(1);
			}
		}, 22000);
	}

	private void startGroundTask() {
		cancelSkillTask();
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21172, 60, 100, true)));
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21173, 60, 100, true)));
		}, 3000, 20000);
	}

	private void electrocute() {
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21176, 60, 100, true)));
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21229, 60, 100, true)));
	}

	@Override
	public void onEndUseSkill(NpcSkillEntry usedSkill) {
		switch (usedSkill.getSkillId()) {
			case 21179:
				Creature creature = getAggroList().getMostHated();
				if (creature != null) {
					spawn(284385, creature.getX(), creature.getY(), creature.getZ(), creature.getHeading());
				}
				break;
			case 21165:
				ThreadPoolManager.getInstance().schedule(() -> {
					if (up) {
						World.getInstance().updatePosition(getOwner(), 255.49063f, 293.35785f, 253.79933f, (byte) 90);
					} else {
						World.getInstance().updatePosition(getOwner(), 255.98627f, 259.0136f, 241.73842f, (byte) 90);
					}
					PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_FORCED_MOVE(getOwner(), getOwner()));
					if (stage == 1) {
						spawn(284663, 266.26517f, 273.97614f, 241.54623f, (byte) 83);
						spawn(284663, 266.26517f, 273.97614f, 241.54623f, (byte) 83);
						spawn(284662, 256.57727f, 278.18225f, 241.54623f, (byte) 90);
						spawn(284664, 246.65663f, 275.51996f, 241.54623f, (byte) 96);
					} else if (stage == 2) {
						electrocute();
						stage = 0;
						startStage(1);
					}
				}, 500);
				break;
			case 21170:
				stage = 1;
				rendSpace(true);
				startIceTask();
				ThreadPoolManager.getInstance().schedule(() -> {
					if (canAct()) {
						cancelSkillTask();
						stage = 2;
						rendSpace(false);
					}
				}, 35000);
				break;
		}
	}

	private void startIceTask() {
		cancelSkillTask();
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21179, 1, 100, true)));
		}, 12000, 20000);
	}

	private void cancelSkillTask() {
		if (skillTask != null && !skillTask.isDone()) {
			skillTask.cancel(true);
		}
	}

	private boolean canAct() {
		return (getOwner() != null && !isDead() && !isHome.get());
	}

	@Override
	protected void handleDied() {
		cancelSkillTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelSkillTask();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelSkillTask();
		stage = 0;
		isHome.set(true);
	}
}
