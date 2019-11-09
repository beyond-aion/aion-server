package ai.instance.theHexway;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.ai.Percentage;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.SummonerAI;

/**
 * @author Sykra
 */
@AIName("adjutant_galamat")
public class AdjutantGalamatAI extends SummonerAI {

	private final AtomicBoolean shieldPhase = new AtomicBoolean(false);
	private final AtomicInteger damageInShieldPhase = new AtomicInteger(0);
	private ScheduledFuture<?> damageDistributionTask;
	private ScheduledFuture<?> addsSpawnTask;

	public AdjutantGalamatAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleIndividualSpawnedSummons(Percentage percent) {
		switch (percent.getPercent()) {
			case 60:
			case 20:
				shieldPhase.set(true);
				getOwner().getQueuedSkills().clear();
				getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21799, 65, 100, 0, 25000)));
				addsSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
					if (!getEffectController().isUnderShield()) {
						resetVariablesAndCancelTasks();
						return;
					}
					rndSpawnInRange(219616, 12);
				}, 0, 4000);

				damageDistributionTask = ThreadPoolManager.getInstance().schedule(() -> {
					if (addsSpawnTask != null && !addsSpawnTask.isDone())
						addsSpawnTask.cancel(true);
					if (!getEffectController().isUnderShield())
						return;
					List<Player> playersInRange = getKnownList().getKnownPlayers().values().stream().filter(p -> PositionUtil.isInRange(p, getOwner(), 20))
						.collect(Collectors.toList());
					if (playersInRange.size() > 0) {
						int maxDmgPerPlayer = damageInShieldPhase.getAndSet(0) / playersInRange.size();
						shieldPhase.set(false);
						if (maxDmgPerPlayer > 0) {
							int chanceNotToDie = percent.getPercent() == 60 ? 40 : 20;
							for (Player player : playersInRange) {
								if (!player.isDead()) {
									int dmgPerPlayer = maxDmgPerPlayer;
									if (maxDmgPerPlayer >= player.getLifeStats().getMaxHp() && Rnd.get(100) <= chanceNotToDie)
										dmgPerPlayer = (int) (player.getLifeStats().getMaxHp() * 0.90);
									player.getController().onAttack(getOwner(), dmgPerPlayer, AttackStatus.NORMALHIT);
									if (player.getLifeStats().getCurrentHp() <= dmgPerPlayer) {
										WorldPosition playerPos = player.getPosition();
										NpcActions.delete(spawn(282465, playerPos.getX(), playerPos.getY(), playerPos.getZ(), playerPos.getHeading()));
									}
								}
							}
						}
					}
				}, 25000);
				break;
			case 50:
			case 10:
				resetVariablesAndCancelTasks();
				break;
		}
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		resetVariablesAndCancelTasks();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		resetVariablesAndCancelTasks();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		resetVariablesAndCancelTasks();
	}

	private void resetVariablesAndCancelTasks() {
		damageInShieldPhase.set(0);
		shieldPhase.set(false);
		if (damageDistributionTask != null && !damageDistributionTask.isDone()) {
			damageDistributionTask.cancel(true);
			damageDistributionTask = null;
		}
		if (addsSpawnTask != null && !addsSpawnTask.isDone()) {
			addsSpawnTask.cancel(true);
			addsSpawnTask = null;
		}
	}

	@Override
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
		if (shieldPhase.get())
			damageInShieldPhase.addAndGet(damage);
		return damage;
	}
}
