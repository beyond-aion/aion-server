package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.shield.Shield;
import com.aionemu.gameserver.model.utils3d.Point3D;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Wakizashi, Source
 */
public class ShieldObserver extends ActionObserver {

	private Creature creature;
	private Shield shield;
	private Point3D oldPosition;

	public ShieldObserver(Shield shield, Creature creature) {
		super(ObserverType.MOVE);
		this.creature = creature;
		this.shield = shield;
		WorldPosition lastPos;
		if (creature instanceof Player && (lastPos = ((Player) creature).getMoveController().getLastPositionFromClient()) != null) {
			this.oldPosition = new Point3D(lastPos.getX(), lastPos.getY(), lastPos.getZ());
		} else {
			this.oldPosition = new Point3D(creature.getX(), creature.getY(), creature.getZ());
		}
	}

	@Override
	public void moved() {
		boolean passedThrough = false;

		if (SiegeService.getInstance().getFortress(shield.getId()).isUnderShield())
			if (!(creature.getZ() < shield.getZ() && oldPosition.getZ() < shield.getZ()))
				if (PositionUtil.isInRange(shield, (float) oldPosition.getX(), (float) oldPosition.getY(), (float) oldPosition.getZ(),
					shield.getTemplate().getRadius()) != PositionUtil.isInRange(shield, creature, shield.getTemplate().getRadius()))
					passedThrough = true;

		if (passedThrough && creature.getController().die()) {
			if (creature instanceof Player)
				((Player) creature).getFlyController().endFly(true);
			creature.getObserveController().removeObserver(this);
		} else {
			oldPosition.x = creature.getX();
			oldPosition.y = creature.getY();
			oldPosition.z = creature.getZ();
		}
	}
}
