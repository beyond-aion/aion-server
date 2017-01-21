package ai.instance.tallocsHollow;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("kinquid_debuff")
public class KinquidDebuffAI extends AggressiveNpcAI {

	@Override
	protected void handleCreatureMoved(Creature creature) {
		super.handleCreatureMoved(creature);
		if (creature instanceof Npc && isInRange(creature, 2)) {
			Npc npc = (Npc) creature;
			if (npc.getNpcId() == 215467) {
				SkillEngine.getInstance().getSkill(getOwner(), getNpcId() == 282008 ? 19235 : 19236, 46, getOwner()).useNoAnimationSkill();
			}
		}
	}

}
