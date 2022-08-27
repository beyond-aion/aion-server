package com.aionemu.gameserver.model.instance.instancereward;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.IdgelDomePlayerInfo;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Ritsu, Estrayl
 */
public class IdgelDomeInfo extends AbstractInstancePointsAndKillsReward<IdgelDomePlayerInfo> {

	public IdgelDomeInfo() {
		super(5600, 1120);
		addPointsByRace(Race.ELYOS, 1000);
		addPointsByRace(Race.ASMODIANS, 1000);
	}

	public void teleportToStartPosition(Player player, WorldMapInstance instance) {
		if (player.getRace() == Race.ELYOS) {
			TeleportService.teleportTo(player, instance.getMapId(), instance.getInstanceId(), 269.76874f, 348.35953f, 79.44365f, (byte) 105);
		} else {
			TeleportService.teleportTo(player, instance.getMapId(), instance.getInstanceId(), 259.3971f, 169.18243f, 79.430855f, (byte) 45);
		}
	}

	public int getMythicKunaxEquipment(Player player) {
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
				return 0;
		}
		int[] possibleRewards = Rnd.chance() < 17.5f ? weapons : armor;
		return Rnd.get(possibleRewards);
	}
}
