package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.ai.BombTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xTz, Sykra
 */
@AIName("bomb")
public class BombAI extends AggressiveNpcAI {

	private BombTemplate template;

	private final List<Future<?>> tasks = new ArrayList<>();

	public BombAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		template = DataManager.AI_DATA.getAiTemplate(getNpcId()).getBombs().getBombTemplate();
		addTask(ThreadPoolManager.getInstance().schedule(() -> useSkill(template.getSkillId()), template.getCd() + 2000));
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTasks();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTasks();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, REWARD_AP_XP_DP_LOOT, REWARD_LOOT -> false;
			default -> super.ask(question);
		};
	}

	private void addTask(Future<?> task) {
		if (task == null)
			return;
		synchronized (tasks) {
			tasks.add(task);
		}
	}

	private void cancelTasks() {
		synchronized (tasks) {
			for (Future<?> task : tasks)
				if (task != null && !task.isDone())
					task.cancel(true);
			tasks.clear();
		}
	}

	private void useSkill(int skill) {
		AIActions.targetSelf(this);
		AIActions.useSkill(this, skill);
		int duration = DataManager.SKILL_DATA.getSkillTemplate(skill).getDuration();
		addTask(ThreadPoolManager.getInstance().schedule(() -> AIActions.deleteOwner(BombAI.this), duration != 0 ? duration + 4000 : 0));
	}

}
