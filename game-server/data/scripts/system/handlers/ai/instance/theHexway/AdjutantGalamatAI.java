package ai.instance.theHexway;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.ai.Percentage;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.SummonerAI;

/**
 * @author Sykra
 */
@AIName("adjutant_galamat")
public class AdjutantGalamatAI extends SummonerAI {

	private static final Logger log = LoggerFactory.getLogger(AdjutantGalamatAI.class);
	private final AtomicBoolean shieldPhase = new AtomicBoolean(false);
	private final AtomicInteger damageInShieldPhase = new AtomicInteger(0);
	private ScheduledFuture<?> shieldPhaseEvaluationTask;
	private ScheduledFuture<?> addSpawnTask;
	private float damageMultiplicator = 1.0f;

	public AdjutantGalamatAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleIndividualSpawnedSummons(Percentage percent) {
		switch (percent.getPercent()) {
			case 60:
			case 20:
				getOwner().getQueuedSkills().clear();
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21799, 65, 100, 0, 25000)));
				break;
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate) {
		if (skillTemplate.getSkillId() == 21799) {
			shieldPhase.set(true);
			addSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> rndSpawnInRange(219616, 12), 0, 4000);
			shieldPhaseEvaluationTask = ThreadPoolManager.getInstance().schedule(() -> {
				shieldPhase.set(false);
				if (addSpawnTask != null && !addSpawnTask.isDone())
					addSpawnTask.cancel(true);
				List<Player> playersInRange = getKnownList().getKnownPlayers().values().stream().filter(p -> PositionUtil.isInRange(p, getOwner(), 30))
					.collect(Collectors.toList());
				if (playersInRange.isEmpty())
					return;
				int dmgPerPlayer = damageInShieldPhase.getAndSet(0) / playersInRange.size();
				float multiplicatorToAdd = 0f;
				for (Player player : playersInRange) {
					if (player.isDead()) {
						multiplicatorToAdd += 0.2;
						continue;
					}
					if (dmgPerPlayer > 0 && player.getLifeStats().getCurrentHp() <= dmgPerPlayer)
						multiplicatorToAdd += 0.35;
				}
				synchronized (this) {
					if (damageMultiplicator == 1.0f)
						damageMultiplicator = 1.2f;
					damageMultiplicator += multiplicatorToAdd;
					damageMultiplicator = Math.min(damageMultiplicator, 2.7f);
				}
			}, 25000);
		}
		super.onEndUseSkill(skillTemplate);
	}

	@Override
	protected void handleBackHome() {
		resetVariablesAndCancelTasks();
		super.handleBackHome();
	}

	@Override
	protected void handleDespawned() {
		resetVariablesAndCancelTasks();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		resetVariablesAndCancelTasks();
		super.handleDied();
	}

	private void resetVariablesAndCancelTasks() {
		shieldPhase.set(false);
		damageInShieldPhase.set(0);
		if (shieldPhaseEvaluationTask != null && !shieldPhaseEvaluationTask.isDone()) {
			shieldPhaseEvaluationTask.cancel(true);
			shieldPhaseEvaluationTask = null;
		}
		if (addSpawnTask != null && !addSpawnTask.isDone()) {
			addSpawnTask.cancel(true);
			addSpawnTask = null;
		}
	}

	@Override
	public int modifyOwnerDamage(int damage, Creature effected, Effect effect) {
		return (int) (damage * damageMultiplicator);
	}

	@Override
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
		if (shieldPhase.get()) {
			if (getEffectController().findBySkillId(21799) == null) {
				getOwner().getQueuedSkills().clear();
				resetVariablesAndCancelTasks();
			} else {
				damageInShieldPhase.addAndGet(damage);
			}
		}
		return damage;
	}
}
