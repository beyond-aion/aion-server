package ai.instance.empyreanCrucible;

import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * Lets a Crucible NPC run for a while without fighting: away from a creature, or in random directions when there is none. Afterwards the NPC
 * fights on and the given action runs.
 */
public final class CrucibleRun {

	private CrucibleRun() {
	}

	public static void start(NpcAI ai, Creature from, int millis, Runnable after) {
		Npc npc = ai.getOwner();
		npc.getController().cancelCurrentSkill(null);
		npc.getMoveController().abortMove();
		ai.setStateIfNot(AIState.FEAR);
		Future<?> moveTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (npc.isDead())
				return;
			float angle = from != null ? PositionUtil.calculateAngleFrom(from, npc) : Rnd.get(0, 359);
			float distance = npc.getGameStats().getMovementSpeedFloat() * 2.5f;
			Vector3f point = GeoService.getInstance().findMovementCollision(npc, angle, distance);
			npc.getMoveController().moveToPoint(point.getX(), point.getY(), point.getZ());
		}, 0, from != null ? 1000 : 2000);
		ThreadPoolManager.getInstance().schedule(() -> {
			moveTask.cancel(false);
			if (npc.isDead())
				return;
			npc.getMoveController().abortMove();
			ai.setStateIfNot(AIState.IDLE);
			ai.onCreatureEvent(AIEventType.ATTACK, npc);
			if (after != null)
				after.run();
		}, millis);
	}
}
