package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Arrays;
import java.util.Collection;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PortalCooldown;
import com.aionemu.gameserver.model.templates.InstanceCooltime;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author nrg, Neon
 */
public class SM_INSTANCE_INFO extends AionServerPacket {

	private byte updateType; // 0 = reset existing data and write new info, 1 = update team info (leader is always first), 2 = add/overwrite data without resetting other
	private Collection<Player> players; // list of players for which their cooldown info will be sent (as of 4.7 there are client bugs which can crash it on 4+ players)
	private Integer[] instanceIds; // list of instances for which the data should be updated

	public SM_INSTANCE_INFO(byte updateType, Player player, Integer... instanceId) {
		this(updateType, Arrays.asList(player), instanceId);
	}

	public SM_INSTANCE_INFO(byte updateType, Collection<Player> players, Integer... instanceId) {
		this.updateType = updateType;
		this.players = players;
		this.instanceIds =  instanceId.length > 0 ? instanceId : DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimes().keySet().toArray(new Integer[0]);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player activePlayer = con.getActivePlayer();
		writeC(updateType);
		writeD(instanceIds.length == 1 ? DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimeByWorldId(instanceIds[0]).getId() : 0); // cooldown ID if only one instance is updated
		writeC(0x00); // unk1
		writeH(players.size());
		for (Player player : players) {
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
				writeC(cooltime.getRace() == activePlayer.getOppositeRace() ? 0 : 1); // hide flag (1 = show, 0 = hide instance from list)
			}
			writeS(player.getName());
		}
	}
}
