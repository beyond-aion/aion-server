package ai.instance.crucibleChallenge;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;

/**
 * @author xTz
 */
@AIName("barrel")
public class BarrelAI2 extends NpcAI2 {

	@Override
	protected void handleDied() {
		super.handleDied();
		int npcId = 0;
		switch (getNpcId()) {
			case 218560:
				npcId = 218561;
				break;
			case 217840:
				npcId = 217841;
				break;
		}
		float direction = Rnd.get(0, 199) / 100f;
		float x1 = (float) (Math.cos(Math.PI * direction) * 4);
		float y1 = (float) (Math.sin(Math.PI * direction) * 4);
		spawn(npcId, getPosition().getX() + x1, getPosition().getY() + y1, getPosition().getZ(), (byte) 0);
		AI2Actions.deleteOwner(this);
	}
}
