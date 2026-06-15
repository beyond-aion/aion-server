package com.aionemu.gameserver.services.panesterra.ahserion;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Yeats, Estrayl
 */
public class PanesterraTeam {

	private static final WorldPosition ELYOS_ORIGIN_POS = new WorldPosition(110070000, 503.567f, 375.164f, 126.790f, (byte) 30);
	private static final WorldPosition ASMO_ORIGIN_POS = new WorldPosition(120080000, 429.001f, 250.508f, 93.129f, (byte) 60);

	private final List<Integer> teamMembers = new ArrayList<>();
	private final PanesterraFaction faction;
	private WorldPosition originPosition;
	private WorldPosition startPosition;
	private boolean isEliminated;

	public PanesterraTeam(PanesterraFaction faction) {
		this.faction = faction;
		switch (faction) {
			case BELUS -> {
				originPosition = new WorldPosition(400020000, 1024.172f, 1063.969f, 1530.3f, (byte) 90);
				startPosition = new WorldPosition(400030000, 287.727f, 291.105f, 680.106f, (byte) 15);
			}
			case IVY_TEMPLE -> startPosition = new WorldPosition(400020000, 550.663f, 552.074f, 1484.714f, (byte) 15);
			case HIGHLAND_TEMPLE -> startPosition = new WorldPosition(400020000, 551.551f, 1496.771f, 1484.714f, (byte) 105);
			case ALPINE_TEMPLE -> startPosition = new WorldPosition(400020000, 1494.988f, 1495.968f, 1484.714f, (byte) 72);
			case GRANDWEIR_TEMPLE -> startPosition = new WorldPosition(400020000, 1495.438f, 551.718f, 1484.714f, (byte) 45);
			case ASPIDA -> {
				originPosition = new WorldPosition(400040000, 1024.172f, 1063.969f, 1530.3f, (byte) 90);
				startPosition = new WorldPosition(400030000, 288.272f, 731.896f, 680.117f, (byte) 105);
			}
			case NOERREN_TEMPLE -> startPosition = new WorldPosition(400040000, 550.663f, 552.074f, 1484.714f, (byte) 15);
			case BOREALIS_TEMPLE -> startPosition = new WorldPosition(400040000, 551.551f, 1496.771f, 1484.714f, (byte) 105);
			case MYRKREN_TEMPLE -> startPosition = new WorldPosition(400040000, 1494.988f, 1495.968f, 1484.714f, (byte) 72);
			case GLUMVEILEN_TEMPLE -> startPosition = new WorldPosition(400040000, 1495.438f, 551.718f, 1484.714f, (byte) 45);
			case ATANATOS -> {
				originPosition = new WorldPosition(110070000, 503.567f, 375.164f, 126.790f, (byte) 30); // TODO: Change to fortress pos
				startPosition = new WorldPosition(400030000, 728.675f, 735.638f, 680.099f, (byte) 75);
			}
			case MEMORIA_TEMPLE -> startPosition = new WorldPosition(400050000, 550.663f, 552.074f, 1484.714f, (byte) 15);
			case SYBILLINE_TEMPLE -> startPosition = new WorldPosition(400050000, 551.551f, 1496.771f, 1484.714f, (byte) 105);
			case AUSTERITY_TEMPLE -> startPosition = new WorldPosition(400050000, 1494.988f, 1495.968f, 1484.714f, (byte) 72);
			case SERENITY_TEMPLE -> startPosition = new WorldPosition(400050000, 1495.438f, 551.718f, 1484.714f, (byte) 45);
			case DISILLON -> {
				originPosition = new WorldPosition(120080000, 429.001f, 250.508f, 93.129f, (byte) 60); // TODO: Change to fortress pos
				startPosition = new WorldPosition(400030000, 730.642f, 293.440f, 680.118f, (byte) 45);
			}
			case NECROLUCE_TEMPLE -> startPosition = new WorldPosition(400060000, 550.663f, 552.074f, 1484.714f, (byte) 15);
			case ESMERAUDUS_TEMPLE -> startPosition = new WorldPosition(400060000, 551.551f, 1496.771f, 1484.714f, (byte) 105);
			case VOLTAIC_TEMPLE -> startPosition = new WorldPosition(400060000, 1494.988f, 1495.968f, 1484.714f, (byte) 72);
			case ILLUMINATUS_TEMPLE  -> startPosition = new WorldPosition(400060000, 1495.438f, 551.718f, 1484.714f, (byte) 45);
		}
	}

	public void moveTeamMembersToOriginPosition() {
		forEachMember(player -> {
			if (player.getWorldId() == 400030000)
				movePlayerToOriginPosition(player);
		});
	}

	public void forEachMember(Consumer<Player> consumer) {
		for (Integer playerId : teamMembers) {
			Player player = World.getInstance().getPlayer(playerId);
			if (player != null)
				consumer.accept(player);
		}
	}

	public void movePlayerToOriginPosition(Player player) {
		WorldPosition targetPosition = switch (faction) {
			case BELUS, ASPIDA, ATANATOS, DISILLON -> originPosition;
			default -> switch (player.getRace()) {
				case ELYOS -> ELYOS_ORIGIN_POS;
				case ASMODIANS -> ASMO_ORIGIN_POS;
				default -> null;
			};
		};
		if (targetPosition != null)
			TeleportService.teleportTo(player, targetPosition);
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
		if (teamMembers.contains(playerId))
			teamMembers.remove(playerId);
	}

	public boolean isEliminated() {
		return isEliminated;
	}

	public void setIsEliminated(boolean value) {
		isEliminated = value;
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
