package ai.instance.raksang;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("raksang_rubble")
public class RaksangRubbleAI extends AggressiveNpcAI {

	public RaksangRubbleAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> {
			SkillEngine.getInstance().getSkill(getOwner(), 19937, 46, getOwner()).useNoAnimationSkill();
			startLifeTask();
		}, 1000);
	}

	private void startLifeTask() {
		ThreadPoolManager.getInstance().schedule(() -> AIActions.deleteOwner(RaksangRubbleAI.this), 9000);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			default -> super.ask(question);
		};
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		AIActions.deleteOwner(this);
	}

}
