package ai.instance.eternalBastion;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author Estrayl
 */
@AIName("eternal_bastion_snare_turret")
public class EternalBastionSnareTurretAI extends EternalBastionAggressiveNpcAI {

	private final AtomicBoolean isSkillDisabled = new AtomicBoolean();

	public EternalBastionSnareTurretAI(Npc owner) {
		super(owner);
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return ItemAttackType.MAGICAL_FIRE;
	}

	@Override
	public void handleFinishAttack() {
		EmoteManager.emoteStopAttacking(getOwner());
		getOwner().getController().loseAggro(false);
		getOwner().setSkillNumber(0);
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		super.handleCreatureAggro(creature);
		if (isSkillDisabled.compareAndSet(false, true)) {
			SkillEngine.getInstance().getSkill(getOwner(), 21116, 65, creature).useNoAnimationSkill(); // Magnetic Pull
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21116) // Magnetic Pull
			SkillEngine.getInstance().getSkill(getOwner(), 21117, 65, getOwner()).useNoAnimationSkill(); // Blanket Venting
		else if (skillTemplate.getSkillId() == 21117) // Blanket Venting
			SkillEngine.getInstance().applyEffectDirectly(21117, getOwner(), getOwner()); // Work-around since HEROs ignore abnormal states atm
	}

	@Override
	public void onEffectEnd(Effect effect) {
		if (effect != null && effect.getSkillId() == 21117)
			isSkillDisabled.set(false);
	}
}
