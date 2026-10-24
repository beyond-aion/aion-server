package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_POSITION;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillAliasLocation;
import com.aionemu.gameserver.skillengine.model.SkillAliasPosition;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetTeleportEffect")
public class TargetTeleportEffect extends EffectTemplate {

	@XmlAttribute(name = "alias_location")
	protected String loc;

	@Override
	public void applyEffect(Effect effect) {
		if (!(effect.getEffected() instanceof Player player))
			return;
		if (loc == null) { // teleport next to the effector, on the side of the target, facing the effector
			final Creature effector = effect.isReflected() ? effect.getOriginalEffected() : effect.getEffector();
			double radian = Math.toRadians(PositionUtil.calculateAngleFrom(effector, player));
			float x = effector.getX() + (float) Math.cos(radian);
			float y = effector.getY() + (float) Math.sin(radian);
			Vector3f closestCollision = GeoService.getInstance().getClosestCollision(player, x, y, effector.getZ());
			byte heading = PositionUtil.getHeadingTowards(closestCollision.getX(), closestCollision.getY(), effector.getX(), effector.getY());
			moveToPosition(player, closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), heading);
		} else { // teleport to random specified position
			SkillAliasLocation skillAliasLocation = DataManager.SKILL_ALIAS_LOCATION_DATA.getSkillAliasLocation(loc);
			if (skillAliasLocation != null && player.getWorldId() == skillAliasLocation.getWorldId()) {
				SkillAliasPosition position = Rnd.get(skillAliasLocation.getSkillAliasPositionList());
				float z = GeoService.getInstance().getZ(player.getWorldId(), position.getX(), position.getY(), position.getZ(), 0, player.getInstanceId());
				if (Float.isNaN(z)) // no ground below the point
					z = position.getZ();
				moveToPosition(player, position.getX(), position.getY(), z, position.getHeading());
			}
		}
	}

	/**
	 * Differs from retail: retail spawns the player anew at the destination, which deselects the player's target. Moving in place keeps the target and
	 * the camera and lets the skill animation play out. To restore the retail behaviour, respawn the player on the same map without starting spawn
	 * protection.
	 */
	private void moveToPosition(Player player, float x, float y, float z, byte heading) {
		player.getController().cancelCurrentSkill(null);
		player.getMoveController().abortMove();
		World.getInstance().updatePosition(player, x, y, z, heading);
		PacketSendUtility.broadcastToSightedPlayers(player, new SM_POSITION(player), true);
	}
}
