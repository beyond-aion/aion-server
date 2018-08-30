package ai.instance.danuarReliquary;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Ritsu, Luzien, Yeats, Estrayl
 */
@AIName("enraged_queen_modor")
public class EnragedQueenModorAI extends AbstractModorAI {

	private AtomicBoolean isFearEventReady = new AtomicBoolean();
	private AtomicBoolean isFearEventCompleted = new AtomicBoolean();
	private AtomicBoolean isElectricVengeanceReady = new AtomicBoolean();
	private Future<?> electricVengeanceTask;

	public EnragedQueenModorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> PacketSendUtility.broadcastMessage(getOwner(), 1500743), 1500);
		ThreadPoolManager.getInstance().schedule(() -> {
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21171, 60, 100)));
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21169, 60, 100)));
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21181, 60, 100)));
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21174, 60, 100)));
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21175, 60, 100)));
		}, 2000);
		electricVengeanceTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> isElectricVengeanceReady.set(true), 90000, 240000);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (getLifeStats().getHpPercentage() <= 50)
			isFearEventReady.compareAndSet(false, true);
	}

	@Override
	protected void handleGroundSkillSet() {
		if (isFearEventReady.get() && isFearEventCompleted.compareAndSet(false, true)) {
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21268, 60, 100)));
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21269, 60, 100)));
			getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21174, 60, 100)));
		} else {
			if (isElectricVengeanceReady.compareAndSet(true, false)) {
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21176, 60, 100)));
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21229, 60, 100)));
			} else {
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21171, 60, 100)));
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21172, 60, 100)));
			}
			handleAdvancedGroundSkillSet();
		}
	}

	protected void handleAdvancedGroundSkillSet() {
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21173, 60, 100)));
	}

	@Override
	protected void handlePlatformSkillSet() {
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21179, 1, 100))); // Ice Storm
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21174, 60, 100, 0, 12000))); // Malice
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21174, 60, 100))); // Malice
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21175, 60, 100))); // Revenge
	}

	protected void summonAdds() {
		spawn(284660, 266.26517f, 273.97614f, 241.54623f, (byte) 83);
		spawn(284661, 266.26517f, 273.97614f, 241.54623f, (byte) 83);
		spawn(284662, 256.57727f, 278.18225f, 241.54623f, (byte) 90);
		spawn(284664, 246.65663f, 275.51996f, 241.54623f, (byte) 96);
	}

	@Override
	protected void handleDied() {
		cancelTasks(electricVengeanceTask);
		forcePositionUpdate(new WorldPosition(getPosition().getMapId(), 255.5310f, 292.5890f, 253.7162f, (byte) 90));
		super.handleDied();
	}

	@Override
	protected void handleBackHome() {
		cancelTasks(electricVengeanceTask);
		super.handleBackHome();
	}

	@Override
	protected void handleDespawned() {
		cancelTasks(electricVengeanceTask);
		super.handleDespawned();
	}

}
