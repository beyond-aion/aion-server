package com.aionemu.gameserver.controllers.observer;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResult;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas
 */
public class CollisionDieActor extends AbstractCollisionObserver {

	public CollisionDieActor(Creature creature, Spatial geometry) {
		super(creature, geometry, CollisionIntention.MATERIAL.getId(), CheckType.PASS);
	}

	@Override
	public void onMoved(CollisionResults collisionResults) {
		if (collisionResults.size() != 0) {
			if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player player && player.isStaff()) {
				CollisionResult result = collisionResults.getClosestCollision();
				PacketSendUtility.sendMessage(player, "Entered " + result.getGeometry().getName());
			}
			FortressLocation fortress = SiegeService.getInstance().findFortress(creature.getWorldId(), creature.getX(), creature.getY(), creature.getZ());
			if (fortress != null && fortress.isUnderShield() && fortress.getRace() != SiegeRace.getByRace(creature.getRace()))
				kill(creature);
		}
	}

	public static void kill(Creature creature) {
		if (creature.getController().die() && creature instanceof Player player) {
			ThreadPoolManager.getInstance().schedule(() -> {
				Kisk kisk = player.getKisk();
				if (kisk != null && kisk.isActive())
					PlayerReviveService.kiskRevive(player);
				else
					PlayerReviveService.bindRevive(player);
				PacketSendUtility.sendPacket(player, new SM_EMOTION(creature, EmotionType.RESURRECT)); // send to remove res option window
			}, 2850);
		}
	}
}
