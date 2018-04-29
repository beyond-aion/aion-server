package com.aionemu.gameserver.services.panesterra.ahserion;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * Created on October 29th, 2017.
 * Currently only supports Ahserion's Flight.
 * 
 * @author Estrayl
 * @since Beyond AION 4.8
 */
public class PanesterraTeam {

	private List<Integer> teamMembers = new ArrayList<>();
	private PanesterraFaction faction;
	private WorldPosition fortressPosition;
	private WorldPosition startPosition;
	private boolean isEliminated;

	public PanesterraTeam(PanesterraFaction faction) {
		this.faction = faction;
		switch (faction) {
			case BELUS:
				fortressPosition = new WorldPosition(0, 0, 0, 0, (byte) 0);
				startPosition = new WorldPosition(400030000, 287.727f, 291.105f, 680.106f, (byte) 15);
				break;
			case ASPIDA:
				fortressPosition = new WorldPosition(0, 0, 0, 0, (byte) 0);
				startPosition = new WorldPosition(400030000, 288.272f, 731.896f, 680.117f, (byte) 105);
				break;
			case ATANATOS:
				fortressPosition = new WorldPosition(110070000, 503.567f, 375.164f, 126.790f, (byte) 30);
				startPosition = new WorldPosition(400030000, 728.675f, 735.638f, 680.099f, (byte) 75);
				break;
			case DISILLON:
				fortressPosition = new WorldPosition(120080000, 429.001f, 250.508f, 93.129f, (byte) 60);
				startPosition = new WorldPosition(400030000, 730.642f, 293.440f, 680.118f, (byte) 45);
				break;
		}
	}

	public void moveTeamMembersToFortressPosition() {
		for (Integer playerId : teamMembers) {
			Player player = World.getInstance().findPlayer(playerId);
			if (player != null)
				TeleportService.teleportTo(player, fortressPosition);
		}
	}

	public void moveTeamToStartPosition() {
		teamMembers.stream().forEach(playerId -> {
			Player p = World.getInstance().findPlayer(playerId);
			if (p != null)
				movePlayerToStartPosition(p);
		});
	}

	public void movePlayerToStartPosition(Player player) {
		TeleportService.teleportTo(player, startPosition);
	}

	public boolean addTeamMemberIfAbsent(int playerId) {
		return !teamMembers.contains(playerId) && teamMembers.add(playerId);
	}

	public boolean isTeamMember(int playerId) {
		return teamMembers.contains(playerId);
	}

	public boolean removeTeamMember(int playerId) {
		return teamMembers.contains(playerId) && teamMembers.remove(playerId) != null;
	}

	public boolean isEliminated() {
		return isEliminated;
	}

	public void setIsEliminated(boolean value) {
		isEliminated = value;
	}

	public WorldPosition getFortressPosition() {
		return fortressPosition;
	}

	public WorldPosition getStartPosition() {
		return startPosition;
	}

	public int getMemberCount() {
		return teamMembers.size();
	}

	public PanesterraFaction getFaction() {
		return faction;
	}
}
