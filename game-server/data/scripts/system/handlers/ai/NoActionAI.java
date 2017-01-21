package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author ATracer
 */
@AIName("noaction")
public class NoActionAI extends NpcAI {

	@Override
	protected void handleAttack(Creature creature) {
		switch (getOwner().getObjectTemplate().getTribe()) { // hp regen for training dummies
			case TARGETBASFELT_DF1:
			case TARGETBASFELT2_DF1:
			case DUMMY:
			case DUMMY2:
			case LF5_DUMMY1:
			case LF5_DUMMY2:
			case DF5_DUMMY1:
			case DF5_DUMMY2:
				getOwner().getController().loseAggro(true);
		}
	}
}
