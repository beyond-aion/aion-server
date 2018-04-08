package com.aionemu.gameserver.model.instance.instancereward;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.IdgelDomePlayerInfo;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author Ritsu, Estrayl
 */
public class IdgelDomeInfo extends InstanceReward<IdgelDomePlayerInfo> {

	public final static int WIN_AP = 5600;
	public final static int DEFEAT_AP = 1120;
	private final EnumMap<Race, WorldPosition> startPositions = new EnumMap<>(Race.class);
	private int asmodianPoints = 1000;
	private int elyosPoints = 1000;
	private int asmodianKills, elyosKills;

	public IdgelDomeInfo(int mapId, int instanceId) {
		super(mapId, instanceId);
		startPositions.put(Race.ELYOS, new WorldPosition(mapId, 269.76874f, 348.35953f, 79.44365f, (byte) 105));
		startPositions.put(Race.ASMODIANS, new WorldPosition(mapId, 259.3971f, 169.18243f, 79.430855f, (byte) 45));
	}

	public List<IdgelDomePlayerInfo> getPlayerRewardByRace(Race race) {
		return getInstanceRewards().stream().filter(r -> r.getRace() == race).collect(Collectors.toList());
	}

	public int getPointsByRace(Race race) {
		return race == Race.ELYOS ? elyosPoints : asmodianPoints;
	}

	public Race getWinningRace() {
		return elyosPoints >= asmodianPoints ? Race.ELYOS : Race.ASMODIANS;
	}

	public int getKillsByRace(Race race) {
		return race == Race.ELYOS ? elyosKills : asmodianKills;
	}

	public void addPointsByRace(Race race, int points) {
		switch (race) {
			case ELYOS:
				elyosPoints += points;
				break;
			case ASMODIANS:
				asmodianPoints += points;
				break;
		}
		if (elyosPoints < 0)
			elyosPoints = 0;
		if (asmodianPoints < 0)
			asmodianPoints = 0;
	}

	public void incrementKillsByRace(Race race) {
		switch (race) {
			case ELYOS:
				elyosKills++;
				break;
			case ASMODIANS:
				asmodianKills++;
				break;
		}
	}

	public int getAsmodianKills() {
		return asmodianKills;
	}

	public int getElyosKills() {
		return elyosKills;
	}

	public int getAsmodianPoints() {
		return asmodianPoints;
	}

	public int getElyosPoints() {
		return elyosPoints;
	}

	public void teleportToStartPosition(Player player) {
		teleport(player, startPositions.get(player.getRace()));
	}

	public void teleport(Player player, WorldPosition p) {
		TeleportService.teleportTo(player, p.getMapId(), instanceId, p.getX(), p.getY(), p.getZ(), p.getHeading());
	}

	public RewardItem getMythicKunaxEquipment(Player player) {
		int[] weapons;
		int[] armor;
		switch (player.getPlayerClass()) { // armor: Chest, Gloves, Shoulders, Pants, Shoes, Helmet
			case TEMPLAR:
				weapons = new int[] { 100901305, 100001682, 115001702 }; // Greatsword, Sword, Shield
				armor = new int[] { 110601549, 111601512, 112601494, 113601495, 114601502, 125003995 };
				break;
			case GLADIATOR:
				weapons = new int[] { 100901305, 101301215, 101701319 }; // Greatsword, Polearm, Bow
				armor = new int[] { 110601549, 111601512, 112601494, 113601495, 114601502, 125003995 };
				break;
			case RANGER:
				weapons = new int[] { 101701319 }; // Bow
				armor = new int[] { 110301751, 111301689, 112301628, 113301720, 114301757, 125003997 };
				break;
			case ASSASSIN:
				weapons = new int[] { 100001682, 100201455, 101701319 }; // Sword, Dagger, Bow
				armor = new int[] { 110301751, 111301689, 112301628, 113301720, 114301757, 125003997 };
				break;
			case GUNNER:
				weapons = new int[] { 101801170, 101801170, 101901081 }; // Pistol, Pistol, Cannon
				armor = new int[] { 110301751, 111301689, 112301628, 113301720, 114301757, 125003997 };
				break;
			case SORCERER:
				weapons = new int[] { 100601378, 100601378, 100501268 }; // Tome, Tome, Orb
				armor = new int[] { 110101754, 111101579, 112101529, 113101591, 114101625, 125003998 };
				break;
			case SPIRIT_MASTER:
				weapons = new int[] { 100601378, 100501268, 100501268 }; // Tome, Orb, Orb
				armor = new int[] { 110101754, 111101579, 112101529, 113101591, 114101625, 125003998 };
				break;
			case BARD:
				weapons = new int[] { 102001197 };
				armor = new int[] { 110101754, 111101579, 112101529, 113101591, 114101625, 125003998 };
				break;
			case CLERIC:
				weapons = new int[] { 101501304, 100101281, 115001702 }; // Staff, Mace, Shield
				armor = new int[] { 110551084, 111501643, 112501582, 113501661, 114501671, 125003996 };
				break;
			case CHANTER:
				weapons = new int[] { 101501304, 101501304, 100101281, 115001702 }; // Staff, Staff, Mace, Shield
				armor = new int[] { 110551084, 111501643, 112501582, 113501661, 114501671, 125003996 };
				break;
			case RIDER:
				weapons = new int[] { 102100969 };
				armor = new int[] { 110551084, 111501643, 112501582, 113501661, 114501671, 125003996 };
				break;
			default:
				LoggerFactory.getLogger(IdgelDomeInfo.class)
					.warn("Couldn't get mythic Kunax equipment for " + player + ". Rewards for " + player.getPlayerClass() + " are not implemented");
				return null;
		}
		int[] possibleRewards = Rnd.chance() < 17.5f ? weapons : armor;
		return new RewardItem(Rnd.get(possibleRewards), 1);
	}
}
