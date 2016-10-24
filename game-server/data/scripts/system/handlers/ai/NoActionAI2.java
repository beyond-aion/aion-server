package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author ATracer
 */
@AIName("noaction")
public class NoActionAI2 extends NpcAI2 {

	@Override
	protected void handleAttack(Creature creature) {
		switch (getOwner().getObjectTemplate().getTribe()) { // hp regen for training dummies
			case TARGETBASFELT_DF1:
			case TARGETBASFELT2_DF1:
			case DUMMY:
			case DUMMY2:
				getOwner().getController().loseAggro(true);
		}
	}
}
