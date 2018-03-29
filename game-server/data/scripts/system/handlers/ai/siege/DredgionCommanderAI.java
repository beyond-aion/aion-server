package ai.siege;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/*
 * @author Luzien
 */
@AIName("dredgionCommander")
public class DredgionCommanderAI extends SiegeNpcAI {

	public DredgionCommanderAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleOneShot();
	}

	private int getSkill() {
		switch (getNpcId()) {
			case 276649:
				return 17572;
			case 276871:
			case 276872:
				return 18411;
			case 258236:
			case 273343:
				return 18428;
			default:
				return 0;
		}
	}

	@Override
	public int modifyOwnerDamage(int damage, Effect effect) {
		if (effect != null && effect.getStack().equals("DGFI_ONESHOTONEKILL_WARPDR"))
			damage *= SiegeConfig.SIEGE_HEALTH_MULTIPLIER;
		return damage;
	}

	private void scheduleOneShot() {
		int skillId = getSkill();
		if (skillId == 0)
			return;
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!getOwner().isSpawned() || getOwner().isDead())
					return;
				VisibleObject obj = getTarget();
				if (obj instanceof Npc) {
					Npc target = (Npc) obj;
					if (target.getRace() == Race.GCHIEF_DARK || target.getRace() == Race.GCHIEF_LIGHT) {
						if (target.isDead())
							return;
						AIActions.useSkill(DredgionCommanderAI.this, skillId);
						getAggroList().addHate(target, 10000);
					}
				}
				scheduleOneShot();
			}
		}, 45 * 1000);
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP)
			stat.setBaseRate(SiegeConfig.SIEGE_HEALTH_MULTIPLIER);
	}
}
