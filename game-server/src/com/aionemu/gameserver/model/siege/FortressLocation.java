package com.aionemu.gameserver.model.siege;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLegionReward;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeMercenaryZone;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeReward;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneInstance;

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

	public FortressLocation(SiegeLocationTemplate template) {
		super(template);
		siegeRewards = template.getSiegeRewards();
		siegeLegionRewards = template.getSiegeLegionRewards();
		siegeMercenaryZones = template.getSiegeMercenaryZones();
		mercenaries = new LinkedHashMap<>();
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
		checkForBalanceBuff(creature, false);
	}

	@Override
	public void onLeaveZone(Creature creature, ZoneInstance zone) {
		super.onLeaveZone(creature, zone);
		creature.unsetInsideZoneType(ZoneType.SIEGE);
		checkForBalanceBuff(creature, true);

	}

	public void checkForBalanceBuff(Creature creature, boolean removeBuff) {
		if (creature instanceof Player && isVulnerable() && getRace() != SiegeRace.BALAUR && getFactionBalance() != 0) {
			if (removeBuff) {
				for (int i = 8867; i <= 8884; i++) {
					if (creature.getEffectController().hasAbnormalEffect(i)) {
						creature.getEffectController().removeEffect(i);
						if (creature.getRace() == Race.ELYOS)
							PacketSendUtility.sendPacket((Player) creature, SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_LIGHT_GET_OUT_AREA());
						else
							PacketSendUtility.sendPacket((Player) creature, SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_DARK_GET_OUT_AREA());
						break;
					}
				}
			} else {
				int balance = getFactionBalance();
				if (creature.getRace() == Race.ELYOS && balance < 0) {
					SkillEngine.getInstance().applyEffect(8866 + Math.abs(balance), creature, creature);
					PacketSendUtility.sendPacket((Player) creature, SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_LIGHT_GAIN());
				} else if (creature.getRace() == Race.ASMODIANS && balance > 0) {
					SkillEngine.getInstance().applyEffect(8875 + balance, creature, creature);
					PacketSendUtility.sendPacket((Player) creature, SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_DARK_GAIN());
				}
			}
		}
	}

	@Override
	public void clearLocation() {
		forEachCreature(creature -> {
			if (isEnemy(creature)) {
				if (creature instanceof Kisk)
					((Kisk) creature).getController().die();
				else if (creature instanceof Player)
					TeleportService.moveToBindLocation((Player) creature);
			}
		});
	}

	public void addMercenaries(int zoneId, List<VisibleObject> mercs) {
		mercenaries.put(zoneId, mercs);
	}

}
