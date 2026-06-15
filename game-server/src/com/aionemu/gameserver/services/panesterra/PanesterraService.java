package com.aionemu.gameserver.services.panesterra;

import static com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.base.BaseOccupier;
import com.aionemu.gameserver.model.base.BaseType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.FortressLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.siegelocation.SiegeRelatedBases;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.BaseService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeam;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapType;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * Workflow for Panesterra sieges:
 * 1. Stop all outer bases
 * 2. Start artifacts as PEACE
 * 3. Start camps as PEACE or
 * Gab1_StartTimeCheck_03 + _04
 * STR_MSG_LDF5_Gab1_End01
 * TODO-List:
 * - Teleportation to other bases should be deactivated, if the target location is not occupied by the same faction
 * - Basic matchmaking, regarding priority slots
 * - Announcements during prep time
 * - Siege tests
 * 
 * @author Estrayl
 */
public class PanesterraService {

	private static final Logger log = LoggerFactory.getLogger("SIEGE_LOG");

	private final Map<PanesterraFaction, PanesterraTeam> activeFactionTeams = new ConcurrentHashMap<>();

	/**
	 * 1. Stop outer bases
	 * 2. Start faction camps
	 * 3. Despawn default corridors
	 * 4. Spawn corridors
	 * 
	 * @param loc
	 *          - Fortress location
	 */
	public void prepareFortressSiege(FortressLocation loc) {
		createTeams(loc.getLocationId());
		spawnAdvanceCorridors();
		prepareBases(loc.getTemplate().getSiegeRelatedBases());
	}

	private void prepareBases(SiegeRelatedBases relatedBases) {
		if (relatedBases == null)
			return;

		relatedBases.getBaseIds().stream().map(BaseService.getInstance()::getBaseLocation).forEach(baseLoc -> {
			switch (baseLoc.getType()) {
				case PANESTERRA -> BaseService.getInstance().capture(baseLoc.getId(), BaseOccupier.PEACE);
				case PANESTERRA_ARTIFACT -> BaseService.getInstance().start(baseLoc.getId());
				case PANESTERRA_FACTION_CAMP -> BaseService.getInstance().capture(baseLoc.getId(), baseLoc.getTemplate().getDefaultOccupier());
			}
		});
	}

	public void startFortressSiege(FortressLocation loc) {
		SiegeRelatedBases relatedBases = loc.getTemplate().getSiegeRelatedBases();
		if (relatedBases != null) {
			relatedBases.getBaseIds().stream().map(BaseService.getInstance()::getBaseLocation)
				.filter(baseLoc -> baseLoc.getType() == BaseType.PANESTERRA_ARTIFACT)
				.forEach(baseLoc -> BaseService.getInstance().capture(baseLoc.getId(), BaseOccupier.BALAUR));
		}
	}

	public void stopFortressSiege(FortressLocation loc) {
		// Remove Teams
		switch (loc.getLocationId()) {
			case 10111 -> removeTeams(IVY_TEMPLE, HIGHLAND_TEMPLE, ALPINE_TEMPLE, GRANDWEIR_TEMPLE);
			case 10211 -> removeTeams(NOERREN_TEMPLE, BOREALIS_TEMPLE, MYRKREN_TEMPLE, GLUMVEILEN_TEMPLE);
			case 10311 -> removeTeams(MEMORIA_TEMPLE, SYBILLINE_TEMPLE, AUSTERITY_TEMPLE, SERENITY_TEMPLE);
			case 10411 -> removeTeams(NECROLUCE_TEMPLE, ESMERAUDUS_TEMPLE, VOLTAIC_TEMPLE, ILLUMINATUS_TEMPLE);
		}
		// Change base states
		SiegeRelatedBases relatedBases = loc.getTemplate().getSiegeRelatedBases();
		if (relatedBases != null) {
			relatedBases.getBaseIds().stream().map(BaseService.getInstance()::getBaseLocation).forEach(baseLoc -> {
				switch (baseLoc.getType()) {
					case PANESTERRA -> BaseService.getInstance().capture(baseLoc.getId(), BaseOccupier.BALAUR);
					case PANESTERRA_ARTIFACT -> BaseService.getInstance().stop(baseLoc.getId());
					case PANESTERRA_FACTION_CAMP -> BaseService.getInstance().capture(baseLoc.getId(), BaseOccupier.PEACE);
				}
			});
		}
	}

	/**
	 * Up to 100 players from each faction, with a rank of 1-Star Officer or higher, can apply for the fortress siege.
	 * Out of those 100 slots, 1 will be reserved for the Governor, 50 will be reserved for 5-Star Officers and above, and
	 * the remaining 49 slots will be reserved for 1-Star Officers and above.
	 * The preparation time is set to 10 minutes, with the first 5 minutes being dedicated to the ranked applications
	 * and the remaining 5 minutes being open to all eligible applications.
	 * <p/>
	 * While some sources suggest that every slot except the Governor is randomly assigned, the matchmaking system has made
	 * sure to guarantee basic group formations by assigning at least one tank and one healer per theoretical group.
	 * <br/>
	 * <br/>
	 * Note: Starting as of v4.9 Rank-1 players were also allowed to apply after the initial 5 minutes.
	 */
	private void spawnAdvanceCorridors() {
		// Elyos
		PacketSendUtility.broadcastToMap(World.getInstance().getWorldMap(110070000).getMainWorldMapInstance(),
			SM_SYSTEM_MESSAGE.STR_MSG_SVS_INVADE_DIRECT_PORTAL_OPEN());
		// Governor exclusive
		spawnCorridor(SpawnEngine.newSingleTimeSpawn(110070000, 730940, 503.624f, 460.202f, 132.081f, (byte) 90), 257);
		// Advance Corridor for Contributors | Officer 5-Star to Commander
		spawnCorridor(SpawnEngine.newSingleTimeSpawn(110070000, 730942, 490.262f, 409.850f, 126.79f, (byte) 90), 256);
		// Walk of Honor | Officer 1-Star to 4-Star
		spawnCorridor(SpawnEngine.newSingleTimeSpawn(110070000, 731193, 518.142f, 409.967f, 126.79f, (byte) 90), 252);
		// Asmodians
		PacketSendUtility.broadcastToMap(World.getInstance().getWorldMap(120080000).getMainWorldMapInstance(),
			SM_SYSTEM_MESSAGE.STR_MSG_SVS_INVADE_DIRECT_PORTAL_OPEN());
		// Governor exclusive
		spawnCorridor(SpawnEngine.newSingleTimeSpawn(120080000, 730941, 342.298f, 251.135f, 98.553f, (byte) 0), 338);
		// Advance Corridor for Contributors | Officer 5-Star to Commander
		spawnCorridor(SpawnEngine.newSingleTimeSpawn(120080000, 730943, 393.321f, 236.963f, 93.113f, (byte) 0), 337);
		// Walk of Glory | Officer 1-Star to 4-Star
		spawnCorridor(SpawnEngine.newSingleTimeSpawn(120080000, 731194, 393.476f, 263.704f, 93.113f, (byte) 0), 336);
	}

	private void spawnCorridor(SpawnTemplate template, int staticId) {
		template.setStaticId(staticId);
		SpawnEngine.spawnObject(template, 1);
	}

	public void startAhserionRaid() {
		if (Stream.of(10111, 10211, 10311, 10411).anyMatch(id -> SiegeService.getInstance().getSiege(id) != null)) {
			log.error("Ahserion raid cannot be started while any Panesterra fortress is under siege.");
			return;
		}
		createTeams(-1);
		SpawnEngine.spawnObject(SpawnEngine.newSingleTimeSpawn(110070000, 802223, 485.692f, 401.079f, 127.789f, (byte) 0), 1);
		SpawnEngine.spawnObject(SpawnEngine.newSingleTimeSpawn(120080000, 802225, 400.772f, 231.517f, 93.113f, (byte) 30), 1);
		AhserionRaid.getInstance().start();
	}

	public void stopAhserionRaid() {
		AhserionRaid.getInstance().stop();
		if (!activeFactionTeams.isEmpty()) {
			for (PanesterraTeam team : activeFactionTeams.values()) {
				team.setIsEliminated(true);
				team.moveTeamMembersToOriginPosition();
			}
			activeFactionTeams.clear();
		}
	}

	public PanesterraTeam handleTeamElimination(PanesterraFaction faction) {
		PanesterraTeam team = activeFactionTeams.get(faction);
		if (team == null)
			return null; // Using the //base command

		team.setIsEliminated(true);
		team.moveTeamMembersToOriginPosition();
		return team;
	}

	private void createTeams(int siegeId) {
		switch (siegeId) {
			case -1 -> { // Transidium Annex
				if (SiegeService.getInstance().getSiegeLocation(10111).getRace() != SiegeRace.BALAUR)
					activeFactionTeams.put(BELUS, new PanesterraTeam(BELUS));
				if (SiegeService.getInstance().getSiegeLocation(10211).getRace() != SiegeRace.BALAUR)
					activeFactionTeams.put(ASPIDA, new PanesterraTeam(ASPIDA));
				if (SiegeService.getInstance().getSiegeLocation(10311).getRace() != SiegeRace.BALAUR)
					activeFactionTeams.put(ATANATOS, new PanesterraTeam(ATANATOS));
				if (SiegeService.getInstance().getSiegeLocation(10411).getRace() != SiegeRace.BALAUR)
					activeFactionTeams.put(DISILLON, new PanesterraTeam(DISILLON));
			}
			case 10111 -> { // Belus
				activeFactionTeams.put(IVY_TEMPLE, new PanesterraTeam(IVY_TEMPLE));
				activeFactionTeams.put(HIGHLAND_TEMPLE, new PanesterraTeam(HIGHLAND_TEMPLE));
				activeFactionTeams.put(ALPINE_TEMPLE, new PanesterraTeam(ALPINE_TEMPLE));
				activeFactionTeams.put(GRANDWEIR_TEMPLE, new PanesterraTeam(GRANDWEIR_TEMPLE));
			}
			case 10211 -> { // Aspida
				activeFactionTeams.put(NOERREN_TEMPLE, new PanesterraTeam(NOERREN_TEMPLE));
				activeFactionTeams.put(BOREALIS_TEMPLE, new PanesterraTeam(BOREALIS_TEMPLE));
				activeFactionTeams.put(MYRKREN_TEMPLE, new PanesterraTeam(MYRKREN_TEMPLE));
				activeFactionTeams.put(GLUMVEILEN_TEMPLE, new PanesterraTeam(GLUMVEILEN_TEMPLE));
			}
			case 10311 -> { // Atanatos
				activeFactionTeams.put(MEMORIA_TEMPLE, new PanesterraTeam(MEMORIA_TEMPLE));
				activeFactionTeams.put(SYBILLINE_TEMPLE, new PanesterraTeam(SYBILLINE_TEMPLE));
				activeFactionTeams.put(AUSTERITY_TEMPLE, new PanesterraTeam(AUSTERITY_TEMPLE));
				activeFactionTeams.put(SERENITY_TEMPLE, new PanesterraTeam(SERENITY_TEMPLE));
			}
			case 10411 -> { // Disillon
				activeFactionTeams.put(NECROLUCE_TEMPLE, new PanesterraTeam(NECROLUCE_TEMPLE));
				activeFactionTeams.put(ESMERAUDUS_TEMPLE, new PanesterraTeam(ESMERAUDUS_TEMPLE));
				activeFactionTeams.put(VOLTAIC_TEMPLE, new PanesterraTeam(VOLTAIC_TEMPLE));
				activeFactionTeams.put(ILLUMINATUS_TEMPLE, new PanesterraTeam(ILLUMINATUS_TEMPLE));
			}
		}
	}

	private void removeTeams(PanesterraFaction... factions) {
		for (PanesterraFaction faction : factions) {
			PanesterraTeam team = activeFactionTeams.remove(faction);
			team.moveTeamMembersToOriginPosition();
		}
	}

	private void spawnAhserionCorridors(int fortressId) {
		switch (fortressId) {
			case 10111 -> spawnCorridor(SpawnEngine.newSingleTimeSpawn(400020000, 802219, 1024.159f, 1076.24f, 1530.2688f, (byte) 90), 0);
			case 10211 -> spawnCorridor(SpawnEngine.newSingleTimeSpawn(400040000, 802221, 1024.159f, 1076.24f, 1530.2688f, (byte) 90), 0);
			case 10311 -> spawnCorridor(SpawnEngine.newSingleTimeSpawn(400050000, 802223, 1024.159f, 1076.24f, 1530.2688f, (byte) 90), 0);
			case 10411 -> spawnCorridor(SpawnEngine.newSingleTimeSpawn(400060000, 802225, 1024.159f, 1076.24f, 1530.2688f, (byte) 90), 0);
		}
	}

	public void onEnterPanesterra(Player player) {
		int siegeId = getSiegeId(player.getWorldId());
		if (siegeId == 0)
			return;
		// Player is in Transidium Annex or on a map with an active siege
		if (siegeId == -1 || SiegeService.getInstance().isSiegeInProgress(siegeId)) {
			PanesterraTeam team = getTeam(player);
			if (team == null)
				TeleportService.moveToBindLocation(player);
			else if (team.isEliminated())
				team.movePlayerToOriginPosition(player);
			else
				player.setPanesterraFaction(team.getFaction());

		} else {
			// Check if the player's faction owns any related fortress
			PanesterraFaction faction = Stream.of(10111, 10211, 10311, 10411)
				.filter(id -> SiegeService.getInstance().getFortress(id).getRace() == SiegeRace.getByRace(player.getRace()))
				.map(PanesterraFaction::getByFortressId).findFirst().orElse(PEACE);

			if (faction == PEACE)
				TeleportService.moveToBindLocation(player);

			player.setPanesterraFaction(faction);
		}
	}

	private int getSiegeId(int worldId) {
		return switch (WorldMapType.getWorld(worldId)) {
			case BELUS -> 10111; // Belus
			case TRANSIDIUM_ANNEX -> -1; // Transidium Annex
			case ASPIDA -> 10211; // Aspida
			case ATANATOS -> 10311; // Atanatos
			case DISILLON -> 10411; // Disillon
			case null, default -> 0;
		};
	}

	public boolean isAhserionRaidStarted() {
		return AhserionRaid.getInstance().isStarted();
	}

	public int getTeamMemberCount(PanesterraFaction faction) {
		PanesterraTeam team = activeFactionTeams.get(faction);
		return team != null ? team.getMemberCount() : 0;
	}

	public PanesterraTeam getTeam(PanesterraFaction faction) {
		return activeFactionTeams.get(faction);
	}

	public PanesterraTeam getTeam(Player player) {
		for (PanesterraTeam team : activeFactionTeams.values()) {
			if (team.isTeamMember(player.getObjectId()))
				return team;
		}
		return null;
	}

	public boolean teleportToStartPosition(Player player) {
		if (!WorldMapType.isPanesterraMap(player.getWorldId()))
			return false;

		PanesterraTeam team = getTeam(player);
		if (team != null && !team.isEliminated()) {
			team.movePlayerToStartPosition(player);
			return true;
		}
		return false;
	}

	// TODO: Event START
	public boolean reviveInEventLocation(Player player) {
		if (!WorldMapType.isPanesterraMap(player.getWorldId()))
			return false;

		teleportToEventLocation(player);
		return true;
	}

	public void teleportToEventLocation(Player player) {
		teleport(player);
		PanesterraService.getInstance().onEnterPanesterra(player);
	}

	private void teleport(Player player) {
		switch (player.getRace()) {
			case ELYOS -> {
				// North + South
				WorldPosition pos = Rnd.nextBoolean() ? new WorldPosition(400020000, 11.173f, 1024.187f, 1428.60f, (byte) 0)
					: new WorldPosition(400020000, 2037.754f, 1023.808f, 1428.60f, (byte) 0);
				TeleportService.teleportTo(player, pos);
			}
			case ASMODIANS -> {
				// West + East
				WorldPosition pos = Rnd.nextBoolean() ? new WorldPosition(400020000, 1023.702f, 10.531f, 1428.60f, (byte) 90)
					: new WorldPosition(400020000, 1024.310f, 2036.593f, 1428.60f, (byte) 90);
				TeleportService.teleportTo(player, pos);
			}
		}
	}
	// TODO: Event END

	public static PanesterraService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {

		protected static final PanesterraService INSTANCE = new PanesterraService();
	}
}
