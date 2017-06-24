package ai.siege;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/*
 * @author Luzien
 */
@AIName("dredgionCommander")
public class DredgionCommanderAI extends SiegeNpcAI {

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

	private void scheduleOneShot() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getSkill() != 0) {
					if (getTarget() instanceof Npc) {
						Npc target = (Npc) getTarget();
						Race race = target.getRace();
						if ((race.equals(Race.GCHIEF_DARK) || race.equals(Race.GCHIEF_LIGHT)) && !target.isDead()) {
							AIActions.useSkill(DredgionCommanderAI.this, getSkill());
							getAggroList().addHate(target, 10000);
						}
					}
					scheduleOneShot();
				}
			}
		}, 45 * 1000);
	}
}
