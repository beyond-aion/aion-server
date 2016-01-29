package ai.instance.drakenspire;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Estrayl
 */
@AIName("seal_guardian")
public class SealGuardianAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleDied() {
		super.handleDied();
		for (Player player : getOwner().getKnownList().getVisiblePlayers().values()) {
			if (isInRange(player, 10))
				SkillEngine.getInstance().applyEffect(21625, getOwner(), player);
		}
		getOwner().getController().onDelete();
	}
}
