package ai.instance.empyreanCrucible;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.aionemu.gameserver.controllers.attack.AggroTarget;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * Base for the Empyrean Crucible bosses, which cast nothing at random: every skill sits on a fight timer or an HP threshold. The fight clock
 * starts on the first hit and resets when the boss leaves the fight; every threshold fires once per fight.
 */
public abstract class CrucibleBossAI extends AggressiveNpcAI {

	private final Set<Integer> passedThresholds = ConcurrentHashMap.newKeySet();
	private Future<?> tickTask;
	private int fightSeconds;

	public CrucibleBossAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		synchronized (this) {
			if (tickTask != null || isDead())
				return;
			fightSeconds = 0;
			passedThresholds.clear();
			tickTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::tick, 1000, 1000);
		}
		onFightStart();
	}

	private void tick() {
		if (isDead())
			return;
		onFightTick(++fightSeconds, getLifeStats().getHpPercentage());
	}

	public synchronized boolean isInFight() {
		return tickTask != null;
	}

	/**
	 * Called once when the fight starts.
	 */
	protected void onFightStart() {
	}

	/**
	 * Called every second of the fight.
	 */
	protected abstract void onFightTick(int seconds, int hpPercentage);

	/**
	 * Called when the boss leaves the fight, dies or despawns.
	 */
	protected void onFightEnd() {
	}

	/**
	 * @return True once per fight, as soon as the HP fall to the given percentage.
	 */
	protected boolean passed(int hpPercentage) {
		return getLifeStats().getHpPercentage() <= hpPercentage && passedThresholds.add(hpPercentage);
	}

	/**
	 * @return True at second {@code first} of the fight and every {@code interval} seconds after it.
	 */
	protected static boolean every(int seconds, int first, int interval) {
		return seconds >= first && (seconds - first) % interval == 0;
	}

	protected void cast(int skillId, int level) {
		getOwner().queueSkill(skillId, level);
	}

	protected void cast(int skillId, int level, NpcSkillTargetAttribute target) {
		getOwner().queueSkill(skillId, level, 0, target);
	}

	/**
	 * Casts a skill on the boss itself right away, also outside the fight.
	 */
	protected void castOnSelf(int skillId, int level) {
		Skill skill = SkillEngine.getInstance().getSkill(getOwner(), skillId, level, getOwner());
		if (skill != null)
			skill.useNoAnimationSkill();
	}

	protected void say(int msgId) {
		PacketSendUtility.broadcastMessage(getOwner(), msgId);
	}

	protected Creature randomAttacker() {
		return getAggroList().getTarget(AggroTarget.RANDOM, 50);
	}

	protected Creature attacker(AggroTarget target) {
		return getAggroList().getTarget(target, 50);
	}

	protected Creature weakestAttacker() {
		return getAggroList().streamValidTargets(50).min(Comparator.comparingInt(c -> c.getLifeStats().getHpPercentage())).orElse(null);
	}

	/**
	 * Makes the given creature the most hated one, so the next skills aimed at the target hit it.
	 */
	protected void switchTarget(Creature creature) {
		if (creature != null)
			getAggroList().addHate(creature, 10000);
	}

	private void stopFight() {
		synchronized (this) {
			if (tickTask == null)
				return;
			tickTask.cancel(false);
			tickTask = null;
		}
		onFightEnd();
	}

	@Override
	protected void handleNotAtHome() {
		stopFight();
		super.handleNotAtHome();
	}

	@Override
	protected void handleBackHome() {
		stopFight();
		super.handleBackHome();
	}

	@Override
	protected void handleDied() {
		stopFight();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		stopFight();
		super.handleDespawned();
	}
}
