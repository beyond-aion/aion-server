package ai.instance.eternalBastion;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.animations.AttackHandAnimation;
import com.aionemu.gameserver.model.animations.AttackTypeAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Estrayl
 */
@AIName("eternal_bastion_dragon")
public class EternalBastionDragonAI extends EternalBastionAggressiveNpcAI {

	private double dist;

	public EternalBastionDragonAI(Npc owner) {
		super(owner);
	}

	@Override
	public AttackTypeAnimation getAttackTypeAnimation(Creature target) {
		dist = PositionUtil.getDistance(getOwner(), target) - getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide()
			- target.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide();
		return dist > 6 ? AttackTypeAnimation.RANGED : AttackTypeAnimation.MELEE;
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		return effect == null && dist > 6 ? damage * 1.2f : damage;
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return dist > 6 ? ItemAttackType.MAGICAL_EARTH : ItemAttackType.PHYSICAL;
	}

	@Override
	public AttackHandAnimation modifyAttackHandAnimation(AttackHandAnimation attackHandAnimation) {
		return Rnd.get(AttackHandAnimation.values());
	}

	@Override
	public boolean ask(AIQuestion question) {
		if (question == AIQuestion.IS_IMMUNE_TO_ABNORMAL_STATES)
			return true;
		return super.ask(question);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		spawn(284697, getPosition().getX(), getPosition().getY(), getPosition().getZ(), (byte) 0);
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21239)
			getAggroList().addHate(Rnd.get(getKnownList().getKnownPlayers().values().stream().toList()), 50000); // Gnaw
	}
}
