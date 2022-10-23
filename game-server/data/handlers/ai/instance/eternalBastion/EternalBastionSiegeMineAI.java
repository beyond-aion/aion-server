package ai.instance.eternalBastion;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ViAl, Estrayl
 */
@AIName("eternal_bastion_siege_mine")
public class EternalBastionSiegeMineAI extends EternalBastionConstructAI {

	private final AtomicBoolean isActivated = new AtomicBoolean();

	public EternalBastionSiegeMineAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isActivated.compareAndSet(false, true))
			SkillEngine.getInstance().getSkill(getOwner(), 21143, 1, getOwner()).useWithoutPropSkill();
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21143) {
			ThreadPoolManager.getInstance().schedule(() -> SkillEngine.getInstance().getSkill(getOwner(), 20549, 1, getOwner()).useSkill(), 750);
			spawn(getNpcId() == 831330 ? 284696 : 284695, getPosition().getX(), getPosition().getY(), getPosition().getZ() + 1, (byte) 0);
		}
	}
}
