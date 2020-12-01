package ai.worlds.pernon;

import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.controllers.observer.GaleCycloneObserver;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author xTz
 */
@AIName("gale_cyclone")
public class GaleCycloneAI extends NpcAI {

	private ConcurrentHashMap<Integer, GaleCycloneObserver> observed = new ConcurrentHashMap<>();
	private boolean blocked;

	public GaleCycloneAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		if (blocked) {
			return;
		}
		if (creature instanceof Player) {
			final Player player = (Player) creature;
			final GaleCycloneObserver observer = new GaleCycloneObserver(player, getOwner()) {

				@Override
				public void onMove() {
					if (!blocked) {
						SkillEngine.getInstance().getSkill(getOwner(), 20528, 50, player).useNoAnimationSkill();
					}
				}

			};
			player.getObserveController().addObserver(observer);
			observed.put(player.getObjectId(), observer);
		}
	}

	@Override
	protected void handleCreatureNotSee(Creature creature) {
		if (blocked) {
			return;
		}
		if (creature instanceof Player) {
			Player player = (Player) creature;
			int obj = player.getObjectId();
			GaleCycloneObserver observer = observed.remove(obj);
			if (observer != null) {
				player.getObserveController().removeObserver(observer);
			}
		}
	}

	@Override
	protected void handleDied() {
		clear();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		clear();
		super.handleDespawned();
	}

	private void clear() {
		blocked = true;
		for (Integer obj : observed.keySet()) {
			Player player = getKnownList().getPlayer(obj);
			GaleCycloneObserver observer = observed.remove(obj);
			if (player != null) {
				player.getObserveController().removeObserver(observer);
			}
		}
	}
}
