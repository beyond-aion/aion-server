package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.StateEvents;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.handler.FollowEventHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * @author ATracer
 */
@AIName("following")
public class FollowingNpcAI2 extends GeneralNpcAI2 {

	@Override
	protected void handleFollowMe(Creature creature) {
		FollowEventHandler.follow(this, creature);
	}
	
	@Override
	protected boolean canHandleEvent(AIEventType eventType) {
		switch (getState()) {
			case DESPAWNED:
				return StateEvents.DESPAWN_EVENTS.hasEvent(eventType);
			case DIED:
				return StateEvents.DEAD_EVENTS.hasEvent(eventType);
			case CREATED:
				return StateEvents.CREATED_EVENTS.hasEvent(eventType);
		}

		if (eventType == AIEventType.CREATURE_MOVED) {
				return getState() == AIState.FOLLOWING;
		}
		return true;
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature == getOwner().getTarget()) {
			FollowEventHandler.creatureMoved(this, creature);
		}
		else if(getOwner().getTarget() == null) {
			FollowEventHandler.stopFollow(this, creature);
		}
	}

	@Override
	protected void handleStopFollowMe(Creature creature) {
		FollowEventHandler.stopFollow(this, creature);
	}
}
