package com.aionemu.gameserver.model.instance.instancereward;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.PlayerClass;
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

	public RewardItem getMythicKunaxEquipmentByPlayerClass(PlayerClass pc) {
		List<Integer> possibleRewards = new ArrayList<>();
		switch (pc) { // Chest, Gloves, Shoulders, Pants, Shoes, Helmet, Weapon, Weapon, Weapon/Shield
			case TEMPLAR: // Greatsword, Sword, Shield
				possibleRewards.addAll(Arrays.asList(110601549, 111601512, 112601494, 113601495, 114601502, 125003995, 100901305, 100001682, 115001702));
				break;
			case GLADIATOR: // Greatsword, Polearm, Bow
				possibleRewards.addAll(Arrays.asList(110601549, 111601512, 112601494, 113601495, 114601502, 125003995, 100901305, 101301215, 101701319));
				break;
			case RANGER: // Bow, Bow, Bow
				possibleRewards.addAll(Arrays.asList(110301751, 111301689, 112301628, 113301720, 114301757, 125003997, 101701319, 101701319, 101701319));
				break;
			case ASSASSIN: // Sword, Dagger, Bow
				possibleRewards.addAll(Arrays.asList(110301751, 111301689, 112301628, 113301720, 114301757, 125003997, 100001682, 100201455, 101701319));
				break;
			case GUNNER: // Pistol, Pistol, Cannon
				possibleRewards.addAll(Arrays.asList(110301751, 111301689, 112301628, 113301720, 114301757, 125003997, 101801170, 101801170, 101901081));
				break;
			case SORCERER: // Tome, Tome, Orb
				possibleRewards.addAll(Arrays.asList(110101754, 111101579, 112101529, 113101591, 114101625, 125003998, 100601378, 100601378, 100501268));
				break;
			case SPIRIT_MASTER: // Tome, Orb, Orb
				possibleRewards.addAll(Arrays.asList(110101754, 111101579, 112101529, 113101591, 114101625, 125003998, 100601378, 100501268, 100501268));
				break;
			case BARD: // Harp, Harp, Harp
				possibleRewards.addAll(Arrays.asList(110101754, 111101579, 112101529, 113101591, 114101625, 125003998, 102001197, 102001197, 102001197));
				break;
			case CLERIC: // Staff, Mace, Shield
				possibleRewards.addAll(Arrays.asList(110551084, 111501643, 112501582, 113501661, 114501671, 125003996, 101501304, 100101281, 115001702));
				break;
			case CHANTER: // Staff, Mace, Shield
				possibleRewards.addAll(Arrays.asList(110551084, 111501643, 112501582, 113501661, 114501671, 125003996, 101501304, 100101281, 115001702));
				break;
			case RIDER: // Key, Key, Key
				possibleRewards.addAll(Arrays.asList(110551084, 111501643, 112501582, 113501661, 114501671, 125003996, 102100969, 102100969, 102100969));
				break;
			default: // just in case
				possibleRewards.add(0);
		}
		return new RewardItem(Rnd.get(possibleRewards), 1);
	}
}
