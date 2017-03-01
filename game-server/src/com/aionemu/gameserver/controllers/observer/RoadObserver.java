package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.road.Road;
import com.aionemu.gameserver.model.templates.road.RoadExit;
import com.aionemu.gameserver.model.utils3d.Point3D;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.WorldType;

/**
 * @author SheppeR
 */
public class RoadObserver extends ActionObserver {

	private Player player;
	private Road road;
	private Point3D oldPosition;

	public RoadObserver() {
		super(ObserverType.MOVE);
		this.player = null;
		this.road = null;
		this.oldPosition = null;
	}

	public RoadObserver(Road road, Player player) {
		super(ObserverType.MOVE);
		this.player = player;
		this.road = road;
		this.oldPosition = new Point3D(player.getX(), player.getY(), player.getZ());
	}

	@Override
	public void moved() {
		Point3D newPosition = new Point3D(player.getX(), player.getY(), player.getZ());
		boolean passedThrough = false;

		if (road.getPlane().intersect(oldPosition, newPosition)) {
			Point3D intersectionPoint = road.getPlane().intersection(oldPosition, newPosition);
			if (intersectionPoint != null) {
				double distance = Math.abs(road.getPlane().getCenter().distance(intersectionPoint));

				if (distance < road.getTemplate().getRadius()) {
					passedThrough = true;
				}
			} else {
				if (PositionUtil.isInRange(road, player, road.getTemplate().getRadius())) {
					passedThrough = true;
				}
			}
		}

		if (passedThrough) {
			RoadExit exit = road.getTemplate().getRoadExit();

			WorldType type = road.getWorldType();
			if (type == WorldType.ELYSEA) {
				if (player.getRace() == Race.ELYOS) {
					TeleportService.teleportTo(player, exit.getMap(), exit.getX(), exit.getY(), exit.getZ(), (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
				}
			} else if (type == WorldType.ASMODAE) {
				if (player.getRace() == Race.ASMODIANS) {
					TeleportService.teleportTo(player, exit.getMap(), exit.getX(), exit.getY(), exit.getZ(), (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
				}
			} else {
				TeleportService.teleportTo(player, exit.getMap(), exit.getX(), exit.getY(), exit.getZ(), (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			}
		}
		oldPosition = newPosition;
	}
}
