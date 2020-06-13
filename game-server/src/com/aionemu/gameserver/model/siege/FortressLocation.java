package com.aionemu.gameserver.model.siege;

import java.util.List;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLegionReward;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeLocationTemplate;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeMercenaryZone;
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

	public FortressLocation(SiegeLocationTemplate template) {
		super(template);
	}

	public List<SiegeLegionReward> getLegionRewards() {
		return getTemplate().getSiegeLegionRewards();
	}

	public List<SiegeMercenaryZone> getSiegeMercenaryZones() {
		return getTemplate().getSiegeMercenaryZones();
	}

	/**
	 * @return isEnemy
	 */
	public boolean isEnemy(Creature creature) {
		return creature.getRace().getRaceId() != getRace().getRaceId();
	}

	@Override
	public void onEnterZone(Creature creature, ZoneInstance zone) {
		super.onEnterZone(creature, zone);
		creature.setInsideZoneType(ZoneType.SIEGE);
		checkForBalanceBuff(creature, SiegeBuff.ADD);
	}

	@Override
	public void onLeaveZone(Creature creature, ZoneInstance zone) {
		super.onLeaveZone(creature, zone);
		creature.unsetInsideZoneType(ZoneType.SIEGE);
		checkForBalanceBuff(creature, SiegeBuff.LEAVE_ZONE_REMOVE);

	}

	public void checkForBalanceBuff(Creature creature, SiegeBuff siegeBuff) {
		if (creature instanceof Player && isVulnerable() && getFactionBalance() != 0) {
			switch (siegeBuff) {
				case LEAVE_ZONE_REMOVE:
				case SIEGE_END_REMOVE:
					for (int i = 8867; i <= 8884; i++) {
						if (creature.getEffectController().hasAbnormalEffect(i)) {
							creature.getEffectController().removeEffect(i);
							if (creature.getRace() == Race.ELYOS) {
								PacketSendUtility.sendPacket((Player) creature, siegeBuff == SiegeBuff.LEAVE_ZONE_REMOVE ?
										SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_LIGHT_GET_OUT_AREA() : SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_LIGHT_MIST_OFF());
							} else {
								PacketSendUtility.sendPacket((Player) creature, siegeBuff == SiegeBuff.LEAVE_ZONE_REMOVE ?
										SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_DARK_GET_OUT_AREA() : SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_DARK_MIST_OFF());
							}
							break;
						}
					}
					break;
				case ADD:
					int balance = getFactionBalance();
					if (creature.getRace() == Race.ELYOS) {
						if (balance < 0) {
							SkillEngine.getInstance().applyEffectDirectly(8866 + Math.abs(balance), creature, creature);
							PacketSendUtility.sendPacket((Player) creature, SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_LIGHT_GAIN());
						} else {
							PacketSendUtility.sendPacket((Player) creature, SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_DARK_WARNING());
						}
					} else if (creature.getRace() == Race.ASMODIANS) {
						if (balance > 0) {
							SkillEngine.getInstance().applyEffectDirectly(8875 + balance, creature, creature);
							PacketSendUtility.sendPacket((Player) creature, SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_DARK_GAIN());
						} else {
							PacketSendUtility.sendPacket((Player) creature, SM_SYSTEM_MESSAGE.STR_MSG_WEAK_RACE_BUFF_LIGHT_WARNING());
						}
					}
					break;
				default:
					break;
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

	public enum SiegeBuff {
		ADD,
		LEAVE_ZONE_REMOVE,
		SIEGE_END_REMOVE
	}
}
