package ai.instance.drakenspire;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Estrayl
 */
@AIName("seal_guardian")
public class SealGuardianAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleDied() {
		super.handleDied();
		for (Player player : getKnownList().getVisiblePlayers().values()) {
			if (isInRange(player, 10)) {
				SkillEngine.getInstance().applyEffect(21625, getOwner(), player);
				break;
			}
		}
		getOwner().getController().onDelete();
	}
}
