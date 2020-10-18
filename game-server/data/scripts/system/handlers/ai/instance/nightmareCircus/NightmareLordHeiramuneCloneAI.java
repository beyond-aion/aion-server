package ai.instance.nightmareCircus;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("nightmarelordheiramuneclone")
public class NightmareLordHeiramuneCloneAI extends AggressiveNpcAI {

	public NightmareLordHeiramuneCloneAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		heal();
	}

	private void heal() {
		Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			Npc boss = getPosition().getWorldMapInstance().getNpc(233467);
			if (boss != null && !boss.isDead()) {
				SkillEngine.getInstance().getSkill(getOwner(), 21342, 1, boss).useSkill();
			} else
				AIActions.deleteOwner(NightmareLordHeiramuneCloneAI.this);
		}, 5000, 10000);
		getOwner().getController().addTask(TaskId.SKILL_USE, task);
	}
}
