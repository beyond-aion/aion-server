package ai.instance.shugoImperialTomb;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("defensetower")
public class DefenseTowerAI extends AggressiveNpcAI {

	private Future<?> task;

	public DefenseTowerAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		if (hpPercentage > 50 && hpPercentage <= 100)
			SkillEngine.getInstance().applyEffectDirectly(21097, getOwner(), getOwner());
		if (hpPercentage > 25 && hpPercentage <= 50)
			SkillEngine.getInstance().applyEffectDirectly(21098, getOwner(), getOwner());
		if (hpPercentage >= 0 && hpPercentage <= 25)
			SkillEngine.getInstance().applyEffectDirectly(21099, getOwner(), getOwner());
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().applyEffectDirectly(21097, getOwner(), getOwner());
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				AIActions.useSkill(DefenseTowerAI.this, 20954);
			}
		}, 2000, 2000);
	}

	@Override
	public void handleDespawned() {
		task.cancel(true);
		super.handleDespawned();
	}

	@Override
	public void handleBackHome() {
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 1;
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
