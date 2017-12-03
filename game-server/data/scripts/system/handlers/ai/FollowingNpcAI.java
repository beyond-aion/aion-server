package ai;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.handler.FollowEventHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 * @author ATracer
 */
@AIName("following")
public class FollowingNpcAI extends GeneralNpcAI {

	public FollowingNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleFollowMe(Creature creature) {
		FollowEventHandler.follow(this, creature);
	}

	@Override
	protected boolean canHandleEvent(AIEventType eventType) {
		switch (eventType) {
			case CREATURE_MOVED:
				return getState() == AIState.FOLLOWING;
			case DIALOG_START:
			case DIALOG_FINISH:
				return getState() == AIState.FOLLOWING || super.canHandleEvent(eventType);
		}
		return super.canHandleEvent(eventType);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature.equals(getOwner().getTarget())) {
			FollowEventHandler.creatureMoved(this, creature);
		} else if (getOwner().getTarget() == null) {
			FollowEventHandler.stopFollow(this, creature);
		}
	}

	@Override
	protected void handleStopFollowMe(Creature creature) {
		FollowEventHandler.stopFollow(this, creature);
	}
}
