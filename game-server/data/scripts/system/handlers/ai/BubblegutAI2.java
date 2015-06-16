package ai;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;



/**
 * @author Luzien
 *
 */
@AIName("bubblegut")
public class BubblegutAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		AI2Actions.useSkill(this,16447);
	}
	
}
