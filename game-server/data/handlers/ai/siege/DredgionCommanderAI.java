package ai.siege;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.services.siege.BalaurAssaultService;
import com.aionemu.gameserver.services.siege.FortressAssault;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Luzien, Estrayl
 */
@AIName("dredgion_commander")
public class DredgionCommanderAI extends SiegeNpcAI {

	private Npc fortressBoss;

	public DredgionCommanderAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(this::findFortressBoss, 3000);
	}

	private void findFortressBoss() {
		for (VisibleObject vo : getKnownList().getKnownObjects().values()) {
			if (vo instanceof Npc boss) {
				if (boss.getRace() == Race.GCHIEF_LIGHT || boss.getRace() == Race.GCHIEF_DARK) {
					fortressBoss = boss;
					getAggroList().addHate(fortressBoss, 500000);
					break;
				}
			}
		}
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getDistanceToSpawnLocation() >= 25.0d && fortressBoss != null) {
			getAggroList().addHate(fortressBoss, 1000000);
			AIActions.targetCreature(this, fortressBoss);
			getOwner().getMoveController().moveToPoint(fortressBoss.getX(), fortressBoss.getY(), fortressBoss.getZ());
		}
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		if (effected == fortressBoss)
			damage *= SiegeConfig.FORTRESS_PROTECTOR_HEALTH_MULTIPLIER;
		return damage;
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP)
			stat.setBaseRate(SiegeConfig.FORTRESS_PROTECTOR_HEALTH_MULTIPLIER);
	}

	@Override
	protected void handleDespawned() {
		fortressBoss = null;
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		FortressAssault assault = BalaurAssaultService.getInstance().getFortressAssaultBySiegeId(((SiegeNpc) getOwner()).getSiegeId());
		if (assault != null)
			assault.onDredgionCommanderKilled();

		fortressBoss = null;
		super.handleDied();
	}
}
