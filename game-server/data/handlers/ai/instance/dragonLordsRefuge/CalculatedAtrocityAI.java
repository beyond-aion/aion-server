package ai.instance.dragonLordsRefuge;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Estrayl
 */
@AIName("calculated_atrocity")
public class CalculatedAtrocityAI extends GeneralNpcAI {

	private Future<?> task;

	public CalculatedAtrocityAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_FIRE;
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return 0;
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		return damage * 0.7f;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();

		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::calculateAndApplyDamage, 500, 2000);

		ThreadPoolManager.getInstance().schedule(() -> AIActions.deleteOwner(this), 11000);
	}

	private void calculateAndApplyDamage() {
		getKnownList().forEachPlayer(p -> {
			if (getOwner().canSee(p) && PositionUtil.isInRange(getOwner(), p, 45) && PositionUtil.isInFrontOf(p, getOwner(), 45))
				SkillEngine.getInstance().applyEffectDirectly(21894, getOwner(), p);
		});
	}

	@Override
	public void handleDespawned() {
		task.cancel(true);
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_DECAY, ALLOW_RESPAWN, REWARD_AP_XP_DP_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
