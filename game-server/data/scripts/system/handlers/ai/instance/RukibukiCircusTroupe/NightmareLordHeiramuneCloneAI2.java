package ai.instance.RukibukiCircusTroupe;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI2;

/**
 * @author Ritsu
 */
@AIName("nightmarelordheiramuneclone")
public class NightmareLordHeiramuneCloneAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		heal();
	}

	private void heal() {
		Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				Npc boss = getPosition().getWorldMapInstance().getNpc(233467);
				if (boss != null && !NpcActions.isAlreadyDead(boss)) {
					SkillEngine.getInstance().getSkill(getOwner(), 21342, 1, boss).useSkill();
				} else
					AI2Actions.deleteOwner(NightmareLordHeiramuneCloneAI2.this);
			}
		}, 5000, 10000);
		getOwner().getController().addTask(TaskId.SKILL_USE, task);
	}
}
