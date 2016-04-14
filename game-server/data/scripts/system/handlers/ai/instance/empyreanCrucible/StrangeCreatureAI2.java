package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI2;

/**
 * @author Luzien
 */
@AIName("strange_creature")
public class StrangeCreatureAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		startEventTask();
		startLifeTask();
	}

	private void startEventTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					PacketSendUtility.broadcastMessage(getOwner(), 341444);
					SkillEngine.getInstance().getSkill(getOwner(), 17914, 34, getOwner()).useNoAnimationSkill();
				}
			}

		}, 500);
	}

	private void startLifeTask() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.deleteOwner(StrangeCreatureAI2.this);
			}

		}, 6500);
	}

}
