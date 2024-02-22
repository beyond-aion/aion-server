package ai.instance.drakenspire;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("orissan_summon_helper")
public class OrissanSummonHelperAI extends GeneralNpcAI {

	public OrissanSummonHelperAI(Npc owner) {
		super(owner);
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 0;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		handleSkillTask();
	}

	private void handleSkillTask() {
		ThreadPoolManager.getInstance().schedule(() -> {
			AIActions.targetSelf(this);
			AIActions.useSkill(this, 21647, 67);
		}, 1000);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		ThreadPoolManager.getInstance().schedule(() -> {
			WorldPosition p = getPosition();
			spawn(getNpcId() == 855607 ? 855699 : 855700, p.getX(), p.getY(), p.getZ(), p.getHeading());
			AIActions.deleteOwner(this);
		}, 1500);
	}

	@Override
	protected void handleBackHome() {
	}

	@Override
	public boolean ask(AIQuestion question) {
		if (question == AIQuestion.REWARD_AP_XP_DP_LOOT)
			return false;
		return super.ask(question);
	}
}
