package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResult;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
public class CollisionDieActor extends AbstractCollisionObserver implements IActor {

	public CollisionDieActor(Creature creature, Spatial geometry) {
		super(creature, geometry, CollisionIntention.MATERIAL.getId(), CheckType.PASS);
	}

	@Override
	public void onMoved(CollisionResults collisionResults) {
		if (collisionResults.size() != 0) {
			if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player) {
				Player player = (Player) creature;
				if (player.isStaff()) {
					CollisionResult result = collisionResults.getClosestCollision();
					PacketSendUtility.sendMessage(player, "Entered " + result.getGeometry().getName());
				}
			}
			FortressLocation fortress = SiegeService.getInstance().findFortress(creature.getWorldId(), creature.getX(), creature.getY(), creature.getZ());
			if (fortress != null && fortress.isUnderShield() && fortress.getRace() != SiegeRace.getByRace(creature.getRace()))
				act();
		}
	}

	@Override
	public void act() {
		if (creature.isInvulnerable())
			return;
		creature.getController().die();
		creature.getController().onStopMove();
	}

	@Override
	public void abort() {
	}
}
