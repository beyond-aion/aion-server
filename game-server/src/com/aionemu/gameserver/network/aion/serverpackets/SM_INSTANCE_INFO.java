package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import javolution.util.FastTable;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PortalCooldown;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.InstanceCooltime;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author nrg
 * @reworked Neon
 */
public class SM_INSTANCE_INFO extends AionServerPacket {

	private byte updateType; // 0 = reset existing data and write new info, 1 = update team info, 2 = overwrite existing data without resetting other
	private Integer[] instanceIds = new Integer[] {}; // if this is set, only portal info for these portals will be sent
	private Collection<Player> players; // list of players for which their cooldown info will be sent to the active player

	public SM_INSTANCE_INFO(Player player, boolean isAnswer, TemporaryPlayerTeam<?> playerTeam) {
		this.updateType = (byte) (playerTeam == null ? 2 : 1);
		this.players = playerTeam == null ? FastTable.of(player) : playerTeam.getMembers();
	}

	public SM_INSTANCE_INFO(Player player, Integer... instanceId) {
		this.updateType = (byte) (instanceId.length > 0 ? 2 : 0);
		this.players = FastTable.of(player);
		this.instanceIds = instanceId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player activePlayer = con.getActivePlayer();
		writeC(updateType);
		writeD(instanceIds.length == 1 ? DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimeByWorldId(instanceIds[0]).getId() : 0); // cooldown ID if only one instance is updated
		writeC(0x00); // unk1
		writeH(players.size());
		for (Player player : players) {
			if (instanceIds.length == 0) { // if no IDs were specified, update dynamically
				if (activePlayer.getObjectId() == player.getObjectId())
					instanceIds = DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimes().keySet().toArray(new Integer[0]); // full instance list
				else
					instanceIds = player.getPortalCooldownList().getPortalCoolDowns().keySet().toArray(new Integer[0]); // only list where player has cooldowns
			}
			writeD(player.getObjectId());
			writeH(instanceIds.length);
			for (int worldId : instanceIds) {
				PortalCooldown cooldown = player.getPortalCooldownList().getPortalCooldown(worldId);
				InstanceCooltime cooltime = DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimeByWorldId(worldId);
				writeD(cooltime.getId());
				writeD(0x00);
				writeD(cooldown == null ? 0 : (int) (cooldown.getReuseTime() - System.currentTimeMillis()) / 1000); // will only be shown from client if entriesUsed == maxEntries
				writeD(cooltime.getMaxCount()); // max entries
				writeD(cooldown == null ? 0 : -cooldown.getEnterCount()); // entry offset (from max)
				writeC(cooltime.getRace() == activePlayer.getCommonData().getOppositeRace() ? 0 : 1); // hide flag (1 = show, 0 = hide instance from list)
			}
			writeS(player.getName());
		}
	}
}
