package ai.instance.sauroBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.ConjunctionType;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

@AIName("guard_captain_ahuradim")
public class GuardCaptainAhuradim extends AggressiveNpcAI {

	private List<Integer> percents = new ArrayList<>();
	private AtomicBoolean started = new AtomicBoolean();
	private int[] generators = new int[]{284437, 284445, 284446};
	private List<Future<?>> scheduledTasks = new ArrayList<>();
	private Future<?> scheduledTask;

	public GuardCaptainAhuradim(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 100:
						if (started.compareAndSet(false, true)) {
							startGeneratorTask();
						}
						break;
					case 75:
					case 50:
					case 25:
						queueSkill(21194,1, 0, NpcSkillTargetAttribute.ME);
						if (Rnd.nextBoolean()) {
							queueSkill(21429, 1, 5000, NpcSkillTargetAttribute.MOST_HATED);
						} else {
							queueSkill(21190, 1, 5000, NpcSkillTargetAttribute.ME);
						}
						break;
				}
				break;
			}
		}
	}

	private void startGeneratorTask() {
		scheduledTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (started.get() && !getOwner().getEffectController().hasAbnormalEffect(21191)) {
				for (VisibleObject obj : getKnownList().getKnownObjects().values()) {
					if (obj instanceof Npc && ((Npc) obj).getNpcId() == 284437) {
						Npc generator = ((Npc) obj);
						generator.setTarget(getOwner());
						PacketSendUtility.broadcastMessage(generator, 1501014);
						scheduledTask = ThreadPoolManager.getInstance().schedule(() -> {
							if (started.get() && generator != null && !generator.isDead()) {
								PacketSendUtility.broadcastMessage(generator, 1501015);
								SkillEngine.getInstance().getSkill(generator, 21200, 1, generator).useWithoutPropSkill();
							}
						}, 15 * 1000);
					}
				}
			}
		}, 40 * 1000);
	}
	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 21194:
				PacketSendUtility.broadcastMessage(getOwner(), 1500786);
				break;
			case 21190:
			case 21429:
				PacketSendUtility.broadcastMessage(getOwner(), 1500785);
				break;
			case 20775:
				PacketSendUtility.broadcastMessage(getOwner(), 1500783);
				break;
		}
	}

	private void queueSkill(int id, int lv, int nextSkillTime, NpcSkillTargetAttribute targetAttribute) {
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(id, lv, 100, 0, nextSkillTime, targetAttribute, 0, 0, 0, 0, ConjunctionType.AND, null)));
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, 100, 75, 50, 25);
	}

	private void cancelTasks() {
		if (scheduledTask != null) {
			scheduledTask.cancel(false);
		}
		if (started.compareAndSet(true, false)) {
			for (Future<?> task : scheduledTasks) {
				task.cancel(false);
			}
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		addPercent();
		cancelTasks();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTasks();
	}
}
