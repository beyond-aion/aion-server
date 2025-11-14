package ai.worlds.pernon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author xTz
 */
@AIName("gale_cyclone")
public class GaleCycloneAI extends NpcAI {

	private final Map<Integer, GaleCycloneObserver> observed = new ConcurrentHashMap<>();

	public GaleCycloneAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		if (creature instanceof Player player) {
			observed.computeIfAbsent(player.getObjectId(), _ -> {
				GaleCycloneObserver galeCycloneObserver = new GaleCycloneObserver(player, getOwner());
				player.getObserveController().addObserver(galeCycloneObserver);
				return galeCycloneObserver;
			});
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
		observed.values().forEach(GaleCycloneObserver::remove);
	}

	private class GaleCycloneObserver extends ActionObserver {

		private final Player player;
		private final Creature creature;
		private double oldRange;

		public GaleCycloneObserver(Player player, Creature creature) {
			super(ObserverType.MOVE);
			this.player = player;
			this.creature = creature;
			oldRange = PositionUtil.getDistance(player, creature);
		}

		@Override
		public void moved() {
			double newRange = PositionUtil.getDistance(player, creature);
			if (creature.isDead() || creature.getLifeStats().isAboutToDie() || !creature.getKnownList().sees(player)) {
				remove();
				return;
			}
			if (oldRange > 12 && newRange <= 12) {
				SkillEngine.getInstance().getSkill(creature, 20528, 50, player).useNoAnimationSkill();
			}
			oldRange = newRange;
		}

		private void remove() {
			player.getObserveController().removeObserver(this);
		}

		@Override
		public void onRemoved() {
			observed.remove(player.getObjectId());
		}
	}
}
