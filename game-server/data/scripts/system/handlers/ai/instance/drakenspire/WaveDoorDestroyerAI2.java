package ai.instance.drakenspire;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("wave_door_destroyer")
public class WaveDoorDestroyerAI2 extends GeneralNpcAI2 {

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		scheduleGateAttack();
	}
	
	private void scheduleGateAttack() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			
			@Override
			public void run() {
				for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs()) {
					if (npc.getNpcId() == 731580) {
						getOwner().setTarget(npc);
						//SkillEngine.getInstance().getSkill(getOwner(), 20838, 1, npc).useSkill();
					}
				}
			}
		}, 20000);
	}
}
