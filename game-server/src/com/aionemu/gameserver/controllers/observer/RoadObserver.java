package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.road.Road;
import com.aionemu.gameserver.model.templates.road.RoadExit;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldType;

/**
 * @author SheppeR
 */
public class RoadObserver extends ActionObserver {

	private final Player player;
	private final Road road;
	private Vector3f oldPosition;

	public RoadObserver(Road road, Player player) {
		super(ObserverType.MOVE);
		this.player = player;
		this.road = road;
		this.oldPosition = new Vector3f(player.getX(), player.getY(), player.getZ());
	}

	@Override
	public void moved() {
		Vector3f newPosition = new Vector3f(player.getX(), player.getY(), player.getZ());
		if (road.isCrossed(oldPosition, newPosition)) {
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
