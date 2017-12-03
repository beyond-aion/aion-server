package ai;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.ai.BombTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xTz
 */
@AIName("bomb")
public class BombAI extends AggressiveNpcAI {

	private BombTemplate template;

	public BombAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		template = DataManager.AI_DATA.getAiTemplate(getNpcId()).getBombs().getBombTemplate();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				doUseSkill();
			}
		}, 2000);
	}

	private void doUseSkill() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				useSkill(template.getSkillId());
			}
		}, template.getCd());
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_REWARD:
				return false;
			default:
				return super.ask(question);
		}
	}

	private void useSkill(int skill) {
		AIActions.targetSelf(this);
		AIActions.useSkill(this, skill);
		int duration = DataManager.SKILL_DATA.getSkillTemplate(skill).getDuration();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AIActions.deleteOwner(BombAI.this);
			}
		}, duration != 0 ? duration + 4000 : 0);
	}
}
