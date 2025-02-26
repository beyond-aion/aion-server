package zone.pvpZones;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.PvPZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.handler.AdvancedZoneHandler;

/**
 * @author MrPoke
 */
public abstract class PvPZone implements AdvancedZoneHandler {

	@Override
	public void onEnterZone(Creature player, ZoneInstance zone) {
	}

	@Override
	public void onLeaveZone(Creature player, ZoneInstance zone) {
	}

	@Override
	public boolean onDie(Creature lastAttacker, Creature target, ZoneInstance zone) {
		if (!(target instanceof Player player))
			return false;

		if (zone instanceof PvPZoneInstance) {
			zone.forEach(creature -> {
				if (creature instanceof Player p) {
					if (p.equals(player))
						PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_MSG_PvPZONE_MY_DEATH_TO_B(lastAttacker.getName()));
					else if (p.equals(lastAttacker))
						PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_MSG_PvPZONE_HOSTILE_DEATH_TO_ME(player.getName()));
					else
						PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_MSG_PvPZONE_HOSTILE_DEATH_TO_B(lastAttacker.getName(), player.getName()));
				}
			});

			player.getController().addTask(TaskId.TELEPORT, ThreadPoolManager.getInstance().schedule(() -> {
				player.getController().getAndRemoveTask(TaskId.TELEPORT); // remove manually as it won't get removed automatically
				PlayerReviveService.duelRevive(player);
				doTeleport(player, zone.getZoneTemplate().getName());
				PacketSendUtility.broadcastToZone(zone, SM_SYSTEM_MESSAGE.STR_PvPZONE_OUT_MESSAGE(player.getName()));
			}, 5000));
		}
		return true;
	}

	protected abstract void doTeleport(Player player, ZoneName zoneName);
}
