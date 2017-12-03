package ai.instance.rakes;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;

import ai.ActionItemNpcAI;

/**
 * @author xTz
 */
@AIName("geniesincenseburner")
public class GeniesIncenseBurnerAI extends ActionItemNpcAI {

	public GeniesIncenseBurnerAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		AIActions.targetSelf(this);
		AIActions.useSkill(this, 18465);
	}
}
