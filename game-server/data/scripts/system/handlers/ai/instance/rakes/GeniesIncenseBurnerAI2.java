package ai.instance.rakes;

import ai.ActionItemNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author xTz
 */
@AIName("geniesincenseburner")
public class GeniesIncenseBurnerAI2 extends ActionItemNpcAI2 {

	@Override
	protected void handleUseItemFinish(Player player) {
		AI2Actions.targetSelf(this);
		AI2Actions.useSkill(this, 18465);
	}
}
