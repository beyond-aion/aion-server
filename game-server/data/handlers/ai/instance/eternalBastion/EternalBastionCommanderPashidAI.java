package ai.instance.eternalBastion;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.animations.AttackHandAnimation;
import com.aionemu.gameserver.model.animations.AttackTypeAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Estrayl
 */
@AIName("eternal_bastion_commander_pashid")
public class EternalBastionCommanderPashidAI extends EternalBastionAggressiveNpcAI {

	private Npc commander;
	private double dist;

	public EternalBastionCommanderPashidAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		spawn(284697, getPosition().getX(), getPosition().getY(), getPosition().getZ(), (byte) 0);
		commander = getPosition().getWorldMapInstance().getNpc(209516);
		if (commander == null)
			commander = getPosition().getWorldMapInstance().getNpc(209517);
		hateCommander(500000);
	}

	private void hateCommander(int hate) {
		if (commander != null && !getOwner().getAggroList().isHating(commander) && PositionUtil.isInRange(getOwner(), commander, 20))
			getOwner().getAggroList().addHate(commander, hate);
	}

	/*
	 * Simulate retail behaviour here.
	 * Pashid and the defense commander will occasionally exchange messages which will result in some shout events and adding mutual hate.
	 */
	@Override
	protected void handleTargetChanged(Creature creature) {
		super.handleTargetChanged(creature);
		ThreadPoolManager.getInstance().schedule(() -> hateCommander(2000000), 2000);
	}

	@Override
	public AttackTypeAnimation getAttackTypeAnimation(Creature target) {
		dist = PositionUtil.getDistance(getOwner(), target) - getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide()
			- target.getObjectTemplate().getBoundRadius().getMaxOfFrontAndSide();
		return dist > 10 ? AttackTypeAnimation.RANGED : AttackTypeAnimation.MELEE;
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		return effect == null && dist > 10 ? damage * 1.3f : damage;
	}

	@Override
	public ItemAttackType modifyAttackType(ItemAttackType type) {
		return dist > 10 ? ItemAttackType.MAGICAL_EARTH : ItemAttackType.PHYSICAL;
	}

	@Override
	public AttackHandAnimation modifyAttackHandAnimation(AttackHandAnimation attackHandAnimation) {
		return Rnd.get(AttackHandAnimation.values());
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		switch (skillTemplate.getSkillId()) {
			case 21239 -> getAggroList().addHate(Rnd.get(getKnownList().getKnownPlayers().values().stream().toList()), 100000); // Gnaw
			case 21236 -> PacketSendUtility.broadcastMessage(getOwner(), 1500757); // Exultation
		}
	}

	@Override
	protected void handleDied() {
		PacketSendUtility.broadcastMessage(getOwner(), 1500759);
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		commander = null;
	}
}
