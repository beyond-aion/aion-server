package com.aionemu.gameserver.model.siege;

import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLegionReward;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeMercenaryZone;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeReward;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.zone.ZoneInstance;

import javolution.util.FastMap;

/**
 * @author Source
 */
public class FortressLocation extends SiegeLocation {

	protected List<SiegeReward> siegeRewards;
	protected List<SiegeLegionReward> siegeLegionRewards;
	protected List<SiegeMercenaryZone> siegeMercenaryZones;
	protected boolean isUnderAssault;
	/**
	 * Zone ID - List of mercenaries
	 */
	protected Map<Integer, List<VisibleObject>> mercenaries;

	public FortressLocation() {
	}

	public FortressLocation(SiegeLocationTemplate template) {
		super(template);
		siegeRewards = template.getSiegeRewards() != null ? template.getSiegeRewards() : null;
		siegeLegionRewards = template.getSiegeLegionRewards() != null ? template.getSiegeLegionRewards() : null;
		siegeMercenaryZones = template.getSiegeMercenaryZones() != null ? template.getSiegeMercenaryZones() : null;
		mercenaries = new FastMap<>();
	}

	public List<SiegeReward> getReward() {
		return siegeRewards;
	}

	public List<SiegeLegionReward> getLegionReward() {
		return siegeLegionRewards;
	}

	public List<SiegeMercenaryZone> getSiegeMercenaryZones() {
		return siegeMercenaryZones;
	}

	/**
	 * @return isEnemy
	 */
	public boolean isEnemy(Creature creature) {
		return creature.getRace().getRaceId() != getRace().getRaceId();
	}

	/**
	 * @return DescriptionId object with fortress name
	 */
	public DescriptionId getNameAsDescriptionId() {
		return new DescriptionId(template.getNameId());
	}

	@Override
	public void onEnterZone(Creature creature, ZoneInstance zone) {
		super.onEnterZone(creature, zone);
		creature.setInsideZoneType(ZoneType.SIEGE);
	}

	@Override
	public void onLeaveZone(Creature creature, ZoneInstance zone) {
		super.onLeaveZone(creature, zone);
		creature.unsetInsideZoneType(ZoneType.SIEGE);
	}

	@Override
	public void clearLocation() {
		forEachCreature(creature -> {
			if (isEnemy(creature)) {
				if (creature instanceof Kisk)
					((Kisk) creature).getController().die();
				else if (creature instanceof Player)
					TeleportService2.moveToBindLocation((Player) creature);
			}
		});
	}

	public void addMercenaries(int zoneId, List<VisibleObject> mercs) {
		mercenaries.put(zoneId, mercs);
	}

}
