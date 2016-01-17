package com.aionemu.gameserver.services.panesterra.ahserion;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

import javolution.util.FastTable;

/**
 * @author Yeats
 *
 */
public class PanesterraTeam {

	protected PanesterraTeamId teamId;
	protected List<Integer> members;
	protected WorldPosition startPosition;
	protected AtomicBoolean isEliminated = new AtomicBoolean(false);
	
	public PanesterraTeam(PanesterraTeamId id) {
		teamId = id;
		members = new FastTable<>();
		setStartPosition();
	}
	
	protected void setStartPosition() {
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
	
	protected void sendPackets(Player player) {
	}
	
	public PanesterraTeamId getTeamId() {
		return teamId;
	}
	
	public List<Integer> getTeamMembers() {
		return members;
	}
	
	public synchronized void removePlayer(Player player) {
		player.setPanesterraTeam(null);
		if (members != null && !members.isEmpty()) {
			members.remove(player.getObjectId());
		}
	}
	
	public void addMember(Player newMember) {
		if (isEliminated.get()) {
			return;
		}
		synchronized (this) {
			if (!members.contains(newMember)) {
				members.add(newMember.getObjectId());
				newMember.setPanesterraTeam(this);
			}
		}
	}

	public WorldPosition getStartPosition() {
		return startPosition;
	}

	public void moveToBindPoint() {
		if (members != null && !members.isEmpty()) {
			Iterator<Integer> iter = members.iterator();
			while (iter.hasNext()) {
				Player player = World.getInstance().findPlayer(iter.next());
				if (player != null) {
					if (player.getPanesterraTeam() != null) {
						player.setPanesterraTeam(null);
					}
					sendPackets(player);
					if (player.getWorldId() == 400030000) {
						TeleportService2.moveToBindLocation(player, true);
						if (player.isInGroup2()) {
							PlayerGroupService.removePlayer(player);
						}
						else if (player.isInAlliance2()) {
							PlayerAllianceService.removePlayer(player);
						}
					}
				}
				iter.remove();
			}
			members.clear();
			members = null;
		}
	}
	
	/**
	 * @param player
	 */
	public void onLeave(Player player) {
		if (player != null) {
			removePlayer(player);
			if (player.isInGroup2()) {
				PlayerGroupService.removePlayer(player);
			}
			else if (player.isInAlliance2()) {
				PlayerAllianceService.removePlayer(player);
			}
			sendPackets(player);
		}
	}
	
	/**
	 * 
	 */
	public void teleportToStartPosition() {
		if (members == null || members.isEmpty() || startPosition == null) {
			return;
		}
		Iterator<Integer> iter = members.iterator();
		while (iter.hasNext()) {
			Player player = World.getInstance().findPlayer(iter.next());
			if (player != null) {
				if (player.isInGroup2()) {
					PlayerGroupService.removePlayer(player);
				}
				else if (player.isInAlliance2()) {
					PlayerAllianceService.removePlayer(player);
				}
				TeleportService2.teleportTo(player, startPosition);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402418));
				sendPackets(player);
			}
		}
	}
	
	public boolean isEliminated() {
		return this.isEliminated.get();
	}
	
	public void setIsEliminated(boolean isEliminated) {
		this.isEliminated.set(isEliminated);
	}

	/**
	 * @return
	 */
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
}
