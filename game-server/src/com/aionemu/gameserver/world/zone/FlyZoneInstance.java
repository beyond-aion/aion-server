package com.aionemu.gameserver.world.zone;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.gameobjects.state.FlyState;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author MrPoke
 */
public class FlyZoneInstance extends ZoneInstance {

	public FlyZoneInstance(int mapId, ZoneInfo template) {
		super(mapId, template);
	}

	@Override
	public synchronized boolean onEnter(Creature creature) {
		if (super.onEnter(creature)) {
			creature.setInsideZoneType(ZoneType.FLY);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized boolean onLeave(Creature creature) {
		if (super.onLeave(creature)) {
			creature.unsetInsideZoneType(ZoneType.FLY);
			if (creature instanceof Player) {
				Player player = (Player) creature;
				if (player.isInFlyingState()) {
					if (player.isInGlidingState()) {
						player.unsetFlyState(FlyState.FLYING);
						player.unsetState(CreatureState.FLYING);
						player.getGameStats().updateStatsAndSpeedVisually();
						PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.STOP_FLY, 0, 0), true);
					} else {// forcefully end fly
						player.getFlyController().endFly(true);
						AuditLogger.info(player, "On leave Fly zone in fly state!!");
					}
				}
			}
			return true;
		} else
			return false;
	}
}
