package com.aionemu.gameserver.services.panesterra.ahserion;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SIEGE_LOCATION_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

import javolution.util.FastTable;

/**
 * @author Yeats
 * @modified Estrayl
 */
public abstract class PanesterraTeam {

	protected PanesterraTeamId teamId;
	protected List<Integer> members = new FastTable<>();
	protected WorldPosition startPosition;
	protected AtomicBoolean isEliminated = new AtomicBoolean(false);

	public PanesterraTeam(PanesterraTeamId id) {
		teamId = id;
		setStartPosition();
	}

	protected abstract void setStartPosition();

	public void teleportToStartPosition() {
		if (members.isEmpty() || startPosition == null)
			return;
		for (Integer id : members) {
			Player player = World.getInstance().findPlayer(id);
			if (player == null)
				continue;
			TeleportService2.teleportTo(player, startPosition);
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_SVS_DIRECT_PORTAL_OPEN_NOTICE());
			sendPackets(player);
		}
	}

	public void moveToBindPoint() {
		if (!members.isEmpty()) {
			for (Integer id : members) {
				Player player = World.getInstance().findPlayer(id);
				if (player == null)
					continue;
				player.setPanesterraTeam(null);
				if (player.getWorldId() == 400030000) {
					ungroupPlayer(player);
					TeleportService2.moveToBindLocation(player);
					sendPackets(player);
				}
			}
			members.clear();
		}
	}

	public void addMember(Player newMember) {
		if (isEliminated.get())
			return;
		synchronized (this) {
			if (!members.contains(newMember.getObjectId())) {
				members.add(newMember.getObjectId());
				newMember.setPanesterraTeam(this);
			}
		}
	}

	public void ungroupPlayer(Player player) {
		if (player.isInGroup2())
			PlayerGroupService.removePlayer(player);
		else if (player.isInAlliance2())
			PlayerAllianceService.removePlayer(player);
	}
	
	protected void sendPackets(Player player) {
		PacketSendUtility.sendPacket(player, new SM_SIEGE_LOCATION_INFO());
	}

	public int getFortressId() {
		switch (teamId) {
			case GAB1_SUB_DEST_69:
				return 10111;
			case GAB1_SUB_DEST_70:
				return 10211;
			case GAB1_SUB_DEST_71:
				return 10311;
			case GAB1_SUB_DEST_72:
				return 10411;
		}
		return 0;
	}

	public PanesterraTeamId getTeamId() {
		return teamId;
	}

	public List<Integer> getMembers() {
		return members;
	}

	public WorldPosition getStartPosition() {
		return startPosition;
	}

	public boolean isEliminated() {
		return this.isEliminated.get();
	}

	public void setIsEliminated(boolean isEliminated) {
		this.isEliminated.set(isEliminated);
	}

	public String getTeamName() {
		switch (teamId) {
			case GAB1_SUB_DEST_69:
				return "BELUS";
			case GAB1_SUB_DEST_70:
				return "ASPIDA";
			case GAB1_SUB_DEST_71:
				return "ATHANOS";
			case GAB1_SUB_DEST_72:
				return "DEYLON";
			default:
				return "";
		}
	}
	
	public int getTeamColor() {
		switch (teamId) {
			case GAB1_SUB_DEST_69:
				return -1962786561;
			case GAB1_SUB_DEST_70:
				return 6260223;
			case GAB1_SUB_DEST_71:
				return 1084100351;
			case GAB1_SUB_DEST_72:
				return -1135668993;
				default:
					return 0;
		}
	}
}
