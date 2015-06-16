package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.MathUtil;

/**
 *
 * @author xTz
 */
public abstract class GaleCycloneObserver extends ActionObserver {

	private Player player;
	private Creature creature;
	private double oldRange;

	public GaleCycloneObserver(Player player, Creature creature) {
		super(ObserverType.MOVE);
		this.player = player;
		this.creature = creature;
		oldRange = MathUtil.getDistance(player, creature);
	}

	@Override
	public void moved() {
		double newRange = MathUtil.getDistance(player, creature);
		if (creature == null || creature.getLifeStats().isAlreadyDead()) {
			if (player != null) {
				player.getObserveController().removeObserver(this);
			}
			return;
		}
		if (oldRange > 12 && newRange <= 12) {
			onMove();
		}
		oldRange = newRange;
	}

	public abstract void onMove();
}
