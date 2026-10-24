package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
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

	@XmlAttribute
	protected int distance; // TODO: find out what this value does. Its not the distance.

	@Override
	public void applyEffect(Effect effect) {
		Creature effected = effect.getEffected();
		if (loc == null) { // teleport in front of effector
			final Creature effector = effect.isReflected() ? effect.getOriginalEffected() : effect.getEffector();
			double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(effector.getHeading()));
			float z = effector.getZ();
			final float x1 = (float) Math.cos(radian);
			final float y1 = (float) Math.sin(radian);
			Vector3f closestCollision = GeoService.getInstance().getClosestCollision(effected,effector.getX() + x1, effector.getY() + y1, z);
			moveToPosition(effected, closestCollision.getX(), closestCollision.getY(), closestCollision.getZ());
		} else { // teleport to random specified position
			SkillAliasLocation skillAliasLocation = DataManager.SKILL_ALIAS_LOCATION_DATA.getSkillAliasLocation(loc);
			if (skillAliasLocation != null && effected.getWorldId() == skillAliasLocation.getWorldId()) {
				SkillAliasPosition position = Rnd.get(skillAliasLocation.getSkillAliasPositionList());
				moveToPosition(effected, position.getX(), position.getY(), position.getZ());
			} else {
				moveToPosition(effected, effected.getX(), effected.getY(), effected.getZ());
			}
		}
	}

	private void moveToPosition(VisibleObject object, float x, float y, float z) {
		if (object instanceof Creature creature) {
			creature.getController().cancelCurrentSkill(null);
			creature.getMoveController().abortMove();
		}
		World.getInstance().updatePosition(object, x, y, z, object.getHeading());
		PacketSendUtility.broadcastToSightedPlayers(object, new SM_POSITION(object), true);
	}
}
