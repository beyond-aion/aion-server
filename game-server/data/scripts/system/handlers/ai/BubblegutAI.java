package ai;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;

/**
 * @author Luzien
 */
@AIName("bubblegut")
public class BubblegutAI extends GeneralNpcAI {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		AIActions.useSkill(this, 16447);
	}

}
