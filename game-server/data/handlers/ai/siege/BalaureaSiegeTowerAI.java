package ai.siege;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;

import ai.AggressiveNpcAI;

/**
 * Protection towers of the Inggison / Gelkmaros fortress protectors
 * retail AI: BGuard_TowerChiefF4{A,B}_Ver47.
 *
 * @author Estrayl
 */
@AIName("balaurea_siege_tower")
public class BalaureaSiegeTowerAI extends AggressiveNpcAI {

	private final AtomicBoolean isGone = new AtomicBoolean();
	private volatile boolean wasInCombat;

	public BalaureaSiegeTowerAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean isMoveSupported() {
		return false;
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP) // scale like the fortress protector (see FortressProtectorNpcAI)
			stat.setBaseRate(SiegeConfig.FORTRESS_PROTECTOR_HEALTH_MULTIPLIER);
	}

	@Override
	protected void handleAttack(Creature creature) {
		wasInCombat = true;
		super.handleAttack(creature);
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		wasInCombat = true;
		super.handleCreatureAggro(creature);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		if (wasInCombat) { // retail: on_leave_attack_state -> broadcast 10032 + despawn_self
			notifyBoss();
			getOwner().getController().delete();
		}
	}

	@Override
	protected void handleDied() {
		notifyBoss();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		notifyBoss();
		super.handleDespawned();
	}

	private void notifyBoss() {
		if (isGone.compareAndSet(false, true) && getCreator() instanceof Npc boss)
			boss.getAi().onCustomEvent(BalaureaFortressProtectorAI.EVENT_TOWER_GONE, getOwner());
	}
}
