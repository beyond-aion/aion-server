package ai.worlds.sarpan;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;


/**
 * @author Cheatkiller
 *
 */
@AIName("giantastriclox")
public class GiantAstricloxAI2 extends NpcAI2 {

	@Override
	public int modifyDamage(Creature creature, int damage) {
		return 1;
	}
	
	@Override
	protected void handleDied() {
		super.handleDied();
		spawn();
	}
	
	private void spawn() {
		spawn(730495, 796.7448f, 867.9318f, 675.22473f, (byte)36);
	  spawn(730495, 794.9168f, 869.0062f, 675.06616f, (byte)34);
	  spawn(730495, 796.2312f, 871.0012f, 674.43726f, (byte)25);
	  spawn(730495, 799.8763f, 869.46265f, 674.75934f, (byte)44);
	  spawn(730495, 802.3064f, 867.8118f, 675.19116f, (byte)46);
	  spawn(730495, 798.8771f, 870.45953f, 674.51013f, (byte)39);
	}
}
