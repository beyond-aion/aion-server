package ai.instance.illuminaryObelisk;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI2;

/**
 * @author M.O.G. Dision
 * @reworked Estrayl
 */
@AIName("dainatoum_healer")
public class DainatumHealerAI2 extends GeneralNpcAI2 {

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		locateBoss();
	}

	private void locateBoss() {
		for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs()) {
			int id = npc.getNpcId();
			if (id == 233740 || id == 284858) {
				getOwner().setTarget(npc);
				scheduleHealTask();
				break;
			}
		}
	}

	private void scheduleHealTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), 21535, 1, getTarget()).useSkill();
					scheduleHealTask();
				}
			}
		}, 10000);
	}
}
