package ai.instance.drakenspire;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Estrayl
 */
@AIName("orissan")
public class OrissanAI extends AggressiveNoLootNpcAI {

	private final AtomicBoolean isActive = new AtomicBoolean();
	private Future<?> task;
	private int lastSpawnEvent; // 1 == Frigid Crystals, 2 == Icing Crystals

	public OrissanAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		lastSpawnEvent = Rnd.get(1, 2);
		// task = ThreadPoolManager.getInstance().schedule(this::startSummoningEvent, 30000);
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		// if (isActive.compareAndSet(false, true))
		// task = ThreadPoolManager.getInstance().schedule(this::startSummoningEvent, 30000);
	}

	private void startSummoningEvent() {
		getOwner().getQueuedSkills().clear();
		getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21646, 1, 100, 0, 10000)));
	}

	@Override
	public void onEndUseSkill(SkillTemplate st, int skillLevel) {
		switch (st.getSkillId()) {
			case 21533: // Ice Explosion
				lastSpawnEvent = 1;
				task = ThreadPoolManager.getInstance().schedule(this::startSummoningEvent, 30000);
				break;
			case 21637: // Rigid Gait
				ThreadPoolManager.getInstance().schedule(this::attackIcingCrystal, 4500);
				break;
			case 21646: // Summon Freezing Crystal
				if (lastSpawnEvent == 1) {
					spawnIcingCrystals();
					task = ThreadPoolManager.getInstance().schedule(() -> {
						getOwner().getEffectController().setAbnormal(AbnormalState.SANCTUARY);
						attackIcingCrystal();
					}, 10000);
				} else {
					spawnFrigidCrystals();
					task = ThreadPoolManager.getInstance().schedule(() -> {
						getOwner().getQueuedSkills().clear();
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21632, 1, 100, 0, 0))); // Frigid Blast
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21633, 1, 100, 0, 8000))); // Ice Explosion
					}, 10000);
				}
				break;
		}
	}

	private void attackIcingCrystal() {
		Npc servant = null;
		for (VisibleObject vo : getKnownList().getKnownObjects().values()) {
			if (vo instanceof Npc && ((Npc) vo).getNpcId() == 855700 && !((Npc) vo).isDead() && !((Creature) vo).getLifeStats().isAboutToDie()) {
				servant = (Npc) vo;
				break;
			}
		}
		if (servant != null) {
			AIActions.targetCreature(this, servant);
			SkillEngine.getInstance().getSkill(getOwner(), 21637, 67, servant).useSkill();
		} else {
			getOwner().getEffectController().unsetAbnormal(AbnormalState.SANCTUARY);
			lastSpawnEvent = 2;
			task = ThreadPoolManager.getInstance().schedule(this::startSummoningEvent, 30000);
		}
	}

	private void spawnIcingCrystals() {
		spawn(855608, 822.0f, 567.7f, 1701.045f, (byte) 0);
		spawn(855608, 811.6f, 577.7f, 1701.045f, (byte) 0);
		spawn(855608, 811.7f, 556.5f, 1701.045f, (byte) 0);
		spawn(855608, 801.1f, 567.9f, 1701.045f, (byte) 0);
	}

	private void spawnFrigidCrystals() {
		spawn(855607, 790.7f, 567.7f, 1701.045f, (byte) 0);
		spawn(855607, 797.0f, 552.0f, 1701.045f, (byte) 0);
		spawn(855607, 797.1f, 583.1f, 1701.045f, (byte) 0);
		spawn(855607, 801.1f, 567.9f, 1701.045f, (byte) 0);
		spawn(855607, 802.5f, 557.9f, 1701.045f, (byte) 0);
		spawn(855607, 802.7f, 576.6f, 1701.045f, (byte) 0);
		spawn(855607, 811.6f, 577.8f, 1701.045f, (byte) 0);
		spawn(855607, 811.6f, 587.7f, 1701.045f, (byte) 0);
		spawn(855607, 811.7f, 556.5f, 1701.045f, (byte) 0);
		spawn(855607, 811.8f, 567.9f, 1701.045f, (byte) 0);
		spawn(855607, 812.0f, 545.5f, 1701.045f, (byte) 0);
		spawn(855607, 820.3f, 557.9f, 1701.045f, (byte) 0);
		spawn(855607, 820.5f, 576.4f, 1701.045f, (byte) 0);
		spawn(855607, 822.0f, 567.7f, 1701.045f, (byte) 0);
		spawn(855607, 826.1f, 551.2f, 1701.045f, (byte) 0);
		spawn(855607, 825.9f, 582.1f, 1701.045f, (byte) 0);
		spawn(855607, 832.8f, 567.6f, 1701.045f, (byte) 0);
	}

	@Override
	protected void handleBackHome() {
		isActive.set(false);
		task.cancel(true);
		super.handleBackHome();
	}

	@Override
	protected void handleDespawned() {
		if (task != null)
			task.cancel(true);
		super.handleDespawned();
	}
}
