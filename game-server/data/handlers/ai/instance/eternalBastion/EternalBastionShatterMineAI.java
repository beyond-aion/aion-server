package ai.instance.eternalBastion;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureSeeState;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Estrayl
 */
@AIName("eternal_bastion_shatter_mine")
public class EternalBastionShatterMineAI extends EternalBastionConstructAI {

	public EternalBastionShatterMineAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		getOwner().setSeeState(CreatureSeeState.SEARCH2);
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		if (creature instanceof Player p && p.isTransformed() && p.getTransformModel().getModelId() == 284320)
			SkillEngine.getInstance().getSkill(getOwner(), 21454, 1, p).useNoAnimationSkill();
	}

}
