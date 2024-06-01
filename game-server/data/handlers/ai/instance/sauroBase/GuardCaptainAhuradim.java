package ai.instance.sauroBase;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

@AIName("guard_captain_ahuradim")
public class GuardCaptainAhuradim extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(100, 75, 50, 25);
	private AtomicBoolean started = new AtomicBoolean();
	private int[] generators = new int[]{284437, 284445, 284446};
	private volatile Future<?> scheduledTask;

	public GuardCaptainAhuradim(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		if (phaseHpPercent == 100) {
			startGeneratorTask();
		} else {
			getOwner().queueSkill(21194,1, 0, NpcSkillTargetAttribute.ME);
			if (Rnd.nextBoolean()) {
				getOwner().queueSkill(21429, 1, 5000);
			} else {
				getOwner().queueSkill(21190, 1, 5000, NpcSkillTargetAttribute.ME);
			}
		}
	}

	private void startGeneratorTask() {
		scheduledTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (hpPhases.getCurrentPhase() > 0 && !getOwner().getEffectController().hasAbnormalEffect(21191)) {
				getPosition().getWorldMapInstance().getNpcs(284437).forEach(generator -> {
					generator.setTarget(getOwner());
					PacketSendUtility.broadcastMessage(generator, 1501014);
					scheduledTask = ThreadPoolManager.getInstance().schedule(() -> {
						if (hpPhases.getCurrentPhase() > 0 && !generator.isDead()) {
							PacketSendUtility.broadcastMessage(generator, 1501015);
							SkillEngine.getInstance().getSkill(generator, 21200, 1, generator).useWithoutPropSkill();
						}
					}, 15 * 1000);
				});
			}
		}, 40 * 1000);
	}

	@Override
	public void onStartUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 20775 -> PacketSendUtility.broadcastMessage(getOwner(), 1500783); // Weaklings! I will destroy you myself!
			case 21190, 21429 -> PacketSendUtility.broadcastMessage(getOwner(), 1500785); // I'm Achradim, the Steel Wall Protector!
			case 21194 -> PacketSendUtility.broadcastMessage(getOwner(), 1500786); // You're very persistent. Allow me to demonstrate my power!
		}
	}

	private void cancelTasks() {
		if (scheduledTask != null) {
			scheduledTask.cancel(false);
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		hpPhases.reset();
		cancelTasks();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTasks();
	}
}
