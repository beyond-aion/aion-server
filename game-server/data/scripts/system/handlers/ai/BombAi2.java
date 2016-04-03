package ai;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.ai.BombTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xTz
 */
@AIName("bomb")
public class BombAi2 extends AggressiveNpcAI2 {

	private BombTemplate template;

	@Override
	protected void handleSpawned() {
		template = DataManager.AI_DATA.getAiTemplate().get(getNpcId()).getBombs().getBombTemplate();
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
		AI2Actions.targetSelf(this);
		AI2Actions.useSkill(this, skill);
		int duration = DataManager.SKILL_DATA.getSkillTemplate(skill).getDuration();
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				AI2Actions.deleteOwner(BombAi2.this);
			}
		}, duration != 0 ? duration + 4000 : 0);
	}
}
