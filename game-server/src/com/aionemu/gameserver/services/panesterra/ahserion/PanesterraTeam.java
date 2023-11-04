package com.aionemu.gameserver.services.panesterra.ahserion;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created on October 29th, 2017.
 * Currently only supports Ahserion's Flight.
 * 
 * @author Estrayl
 * @since Beyond AION 4.8
 */
public class PanesterraTeam {

	private final List<Integer> teamMembers = new ArrayList<>();
	private final PanesterraFaction faction;
	private WorldPosition fortressPosition;
	private WorldPosition startPosition;
	private boolean isEliminated;

	public PanesterraTeam(PanesterraFaction faction) {
		this.faction = faction;
		switch (faction) {
			case BELUS -> {
				fortressPosition = new WorldPosition(0, 0, 0, 0, (byte) 0);
				startPosition = new WorldPosition(400030000, 287.727f, 291.105f, 680.106f, (byte) 15);
			}
			case ASPIDA -> {
				fortressPosition = new WorldPosition(0, 0, 0, 0, (byte) 0);
				startPosition = new WorldPosition(400030000, 288.272f, 731.896f, 680.117f, (byte) 105);
			}
			case ATANATOS -> {
				fortressPosition = new WorldPosition(110070000, 503.567f, 375.164f, 126.790f, (byte) 30);
				startPosition = new WorldPosition(400030000, 728.675f, 735.638f, 680.099f, (byte) 75);
			}
			case DISILLON -> {
				fortressPosition = new WorldPosition(120080000, 429.001f, 250.508f, 93.129f, (byte) 60);
				startPosition = new WorldPosition(400030000, 730.642f, 293.440f, 680.118f, (byte) 45);
			}
		}
	}

	public void moveTeamMembersToFortressPosition() {
		forEachMember(player -> {
			if (player.getWorldId() == 400030000) {
				TeleportService.teleportTo(player, fortressPosition);
			}
		});
	}

	public void forEachMember(Consumer<Player> consumer) {
		for (Integer playerId : teamMembers) {
			Player player = World.getInstance().getPlayer(playerId);
			if (player != null)
				consumer.accept(player);
		}
	}

	public void movePlayerToStartPosition(Player player) {
		TeleportService.teleportTo(player, startPosition);
	}

	public void addTeamMemberIfAbsent(int playerId) {
		if (teamMembers.contains(playerId))
			return;
		teamMembers.add(playerId);
	}

	public boolean isTeamMember(int playerId) {
		return teamMembers.contains(playerId);
	}

	public void removeTeamMember(int playerId) {
			if (teamMembers.contains(playerId)) {
					teamMembers.remove(playerId);
			}
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
