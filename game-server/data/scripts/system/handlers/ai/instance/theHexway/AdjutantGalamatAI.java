package ai.instance.theHexway;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.ai.Percentage;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.SummonerAI;

/**
 * @author Sykra
 */
@AIName("adjutant_galamat")
public class AdjutantGalamatAI extends SummonerAI {

	private AtomicBoolean shieldPhase = new AtomicBoolean(false);
	private AtomicInteger damageInShieldPhase = new AtomicInteger(0);
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
				SkillEngine.getInstance().getSkill(getOwner(), 21799, 65, getOwner()).useNoAnimationSkill();
				addsSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> rndSpawnInRange(219616, 12), 150, 4000);
				final int chanceNotToDie = percent.getPercent() == 60 ? 80 : 20;

				damageDistributionTask = ThreadPoolManager.getInstance().schedule(() -> {
					if (addsSpawnTask != null && !addsSpawnTask.isDone())
						addsSpawnTask.cancel(true);

					Collection<Player> players = getKnownList().getKnownPlayers().values();
					int playersInRange = players.size();
					if (playersInRange > 0) {
						int dmgPerMember = (damageInShieldPhase.get()) / playersInRange;
						damageInShieldPhase.set(0);
						shieldPhase.set(false);
						if (dmgPerMember > 0) {
							for (Player player : players) {
								if (player.getLifeStats().getMaxHp() <= dmgPerMember) {
									int dmg = dmgPerMember;
									if (Rnd.chance() < chanceNotToDie)
										dmg = (int) (player.getLifeStats().getMaxHp() * 0.95);

									player.getController().onAttack(getOwner(), dmg, AttackStatus.NORMALHIT);
									WorldPosition p = player.getPosition();
									Npc smoke = (Npc) spawn(282465, p.getX(), p.getY(), p.getZ(), p.getHeading());
									NpcActions.delete(smoke);
								}
							}
						}
					}
				}, 25 * 1000);
				break;
			case 50:
			case 10:
				resetsVariablesAndCancelTasks();
				break;
		}

	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		resetsVariablesAndCancelTasks();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		resetsVariablesAndCancelTasks();
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		resetsVariablesAndCancelTasks();
	}

	private void resetsVariablesAndCancelTasks() {
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
