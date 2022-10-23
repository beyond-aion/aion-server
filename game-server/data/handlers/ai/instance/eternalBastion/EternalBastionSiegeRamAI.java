package ai.instance.eternalBastion;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author Estrayl
 */
@AIName("eternal_bastion_siege_ram")
public class EternalBastionSiegeRamAI extends EternalBastionAssaulterNpcAI {

	public EternalBastionSiegeRamAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleCreatureAggro(Creature creature) {
		if (creature instanceof Npc) {
			getOwner().getAggroList().addHate(creature, 500000);
			SkillEngine.getInstance().getSkill(getOwner(), 20778, 1, getTarget()).useSkill();
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		SkillEngine.getInstance().getSkill(getOwner(), 20778, 1, getTarget()).useSkill();
	}
}
