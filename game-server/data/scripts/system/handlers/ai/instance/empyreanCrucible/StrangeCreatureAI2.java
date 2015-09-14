package ai.instance.empyreanCrucible;

import ai.GeneralNpcAI2;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;

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
					NpcShoutsService.getInstance().sendMsg(getOwner(), 341444, getObjectId(), 0, 0);
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
