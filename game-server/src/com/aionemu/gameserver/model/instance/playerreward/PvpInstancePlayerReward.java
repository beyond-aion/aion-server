package com.aionemu.gameserver.model.instance.playerreward;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Estrayl
 */
public class PvpInstancePlayerReward extends InstancePlayerReward {

	private final Race race;
	private final AtomicInteger capturedZones = new AtomicInteger();
	private int[] reward1;
	private int[] reward2;
	private int[] reward3;
	private int[] reward4;
	private int[] bonusReward;
	private int baseAp;
	private int bonusAp;
	private int baseGp;
	private int bonusGp;

	public PvpInstancePlayerReward(int objectId, Race race) {
		super(objectId);
		this.race = race;
	}

	public int getReward1ItemId() {
		return reward1 == null ? 0 : reward1[0];
	}

	public int getReward1Count() {
		return reward1 == null ? 0 : reward1[1];
	}

	public int getReward1BonusCount() {
		return reward1 == null ? 0 : reward1[2];
	}

	public void setReward1(int itemId, int count, int bonusCount) {
		reward1 = new int[] { itemId, count, bonusCount };
	}

	public int getReward2ItemId() {
		return reward2 == null ? 0 : reward2[0];
	}

	public int getReward2Count() {
		return reward2 == null ? 0 : reward2[1];
	}

	public int getReward2BonusCount() {
		return reward2 == null ? 0 : reward2[2];
	}

	public void setReward2(int itemId, int count, int bonusCount) {
		reward2 = new int[] { itemId, count, bonusCount };
	}

	public int getReward3ItemId() {
		return reward3 == null ? 0 : reward3[0];
	}

	public int getReward3Count() {
		return reward3 == null ? 0 : reward3[1];
	}

	public void setReward3(int itemId, int count) {
		reward3 = new int[] { itemId, count };
	}

	public int getReward4ItemId() {
		return reward4 == null ? 0 : reward4[0];
	}

	public int getReward4Count() {
		return reward4 == null ? 0 : reward4[1];
	}

	public void setReward4(int itemId, int count) {
		reward4 = new int[] { itemId, count };
	}

	public int getBonusRewardItemId() {
		return bonusReward == null ? 0 : bonusReward[0];
	}

	public int getBonusRewardCount() {
		return bonusReward == null ? 0 : bonusReward[1];
	}

	public void setBonusReward(int itemId, int count) {
		bonusReward = new int[] { itemId, count };
	}

	public int getBaseAp() {
		return baseAp;
	}

	public void setBaseAp(int baseAp) {
		this.baseAp = baseAp;
	}

	public int getBonusAp() {
		return bonusAp;
	}

	public void setBonusAp(int bonusAp) {
		this.bonusAp = bonusAp;
	}

	public int getBaseGp() {
		return baseGp;
	}

	public void setBaseGp(int baseGp) {
		this.baseGp = baseGp;
	}

	public int getBonusGp() {
		return bonusGp;
	}

	public void setBonusGp(int bonusGp) {
		this.bonusGp = bonusGp;
	}

	public void incrementCapturedZones() {
		capturedZones.incrementAndGet();
	}

	public int getCapturedZones() {
		return capturedZones.get();
	}

	public Race getRace() {
		return race;
	}

	public int getMythicKunaxEquipment(Player player) {
		int[] weapons;
		int[] armor;
		switch (player.getPlayerClass()) { // armor: Chest, Gloves, Shoulders, Pants, Shoes, Helmet
			case TEMPLAR -> {
				weapons = new int[] { 100901305, 100001682, 115001702 }; // Greatsword, Sword, Shield
				armor = new int[] { 110601549, 111601512, 112601494, 113601495, 114601502, 125003995 };
			}
			case GLADIATOR -> {
				weapons = new int[] { 100901305, 101301215, 101701319 }; // Greatsword, Polearm, Bow
				armor = new int[] { 110601549, 111601512, 112601494, 113601495, 114601502, 125003995 };
			}
			case RANGER -> {
				weapons = new int[] { 101701319 }; // Bow
				armor = new int[] { 110301751, 111301689, 112301628, 113301720, 114301757, 125003997 };
			}
			case ASSASSIN -> {
				weapons = new int[] { 100001682, 100201455, 101701319 }; // Sword, Dagger, Bow
				armor = new int[] { 110301751, 111301689, 112301628, 113301720, 114301757, 125003997 };
			}
			case GUNNER -> {
				weapons = new int[] { 101801170, 101801170, 101901081 }; // Pistol, Pistol, Cannon
				armor = new int[] { 110301751, 111301689, 112301628, 113301720, 114301757, 125003997 };
			}
			case SORCERER -> {
				weapons = new int[] { 100601378, 100601378, 100501268 }; // Tome, Tome, Orb
				armor = new int[] { 110101754, 111101579, 112101529, 113101591, 114101625, 125003998 };
			}
			case SPIRIT_MASTER -> {
				weapons = new int[] { 100601378, 100501268, 100501268 }; // Tome, Orb, Orb
				armor = new int[] { 110101754, 111101579, 112101529, 113101591, 114101625, 125003998 };
			}
			case BARD -> {
				weapons = new int[] { 102001197 };
				armor = new int[] { 110101754, 111101579, 112101529, 113101591, 114101625, 125003998 };
			}
			case CLERIC -> {
				weapons = new int[] { 101501304, 100101281, 115001702 }; // Staff, Mace, Shield
				armor = new int[] { 110551084, 111501643, 112501582, 113501661, 114501671, 125003996 };
			}
			case CHANTER -> {
				weapons = new int[] { 101501304, 101501304, 100101281, 115001702 }; // Staff, Staff, Mace, Shield
				armor = new int[] { 110551084, 111501643, 112501582, 113501661, 114501671, 125003996 };
			}
			case RIDER -> {
				weapons = new int[] { 102100969 };
				armor = new int[] { 110551084, 111501643, 112501582, 113501661, 114501671, 125003996 };
			}
			default -> {
				LoggerFactory.getLogger(PvpInstancePlayerReward.class)
					.warn("Couldn't get mythic Kunax equipment for " + player + ". Rewards for " + player.getPlayerClass() + " are not implemented");
				return 0;
			}
		}
		int[] possibleRewards = Rnd.chance() < 17.5f ? weapons : armor;
		return Rnd.get(possibleRewards);
	}
}
