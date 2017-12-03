package ai.instance.crucibleChallenge;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author xTz
 */
@AIName("barrel")
public class BarrelAI extends NpcAI {

	public BarrelAI(Npc owner) {
		super(owner);
	}

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
		AIActions.deleteOwner(this);
	}
}
