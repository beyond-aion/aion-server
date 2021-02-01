package com.aionemu.gameserver.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlEnum;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.model.stats.calc.PlayerStatCalculator;
import com.aionemu.gameserver.model.templates.L10n;
import com.aionemu.gameserver.model.templates.stats.CreatureSpeeds;
import com.aionemu.gameserver.model.templates.stats.StatsTemplate;

/**
 * This enum represent class that a player may belong to.
 * 
 * @author Luno
 */
@XmlEnum
public enum PlayerClass implements L10n {
	WARRIOR(0, 240000, true, 110, 110, 100 ,100, 90, 90, 400, 400, 18, 2, 50, 0),
	GLADIATOR(1, 240001, WARRIOR, 115, 115, 100, 100, 90, 90, 440, 400, 18, 2, 50, 0), // fighter
	TEMPLAR(2, 240002, WARRIOR, 115, 100, 100, 100, 90, 105, 460, 400, 18, 2, 50, 0), // knight
	SCOUT(3, 240003, true, 100, 100, 110, 110, 90, 90, 360, 400, 18, 2, 50, 0),
	ASSASSIN(4, 240004, SCOUT, 110, 100, 110, 110, 90, 90, 360, 400, 18, 2, 50, 0),
	RANGER(5, 240005, SCOUT, 100, 100, 115, 115, 90, 90, 280, 400, 18, 2, 50, 0),
	MAGE(6, 240006, true, 90, 90, 95, 95, 115, 115, 260, 600, 18, 2, 50, 0),
	SORCERER(7, 240007, MAGE, 90, 90, 100, 100, 120, 110, 260, 600, 18, 2, 50, 50), // wizard
	SPIRIT_MASTER(8, 240008, MAGE, 90, 90, 100, 100, 115, 115, 280, 600, 18, 2, 50, 50), // elementalist
	PRIEST(9, 240009, true, 95, 95, 100, 100, 100, 100, 360, 600, 18, 2, 50, 0),
	CLERIC(10, 240010, PRIEST, 105, 110, 90, 90, 105, 110, 320, 600, 18, 2, 50, 50),
	CHANTER(11, 240011, PRIEST, 110, 105, 90, 90, 105, 110, 360, 600, 18, 2, 50, 0),
	ENGINEER(12, 904314, true, 100, 100, 110, 110, 90, 90, 360, 400, 18, 2, 50, 0),
	RIDER(13, 904315, ENGINEER, 100, 100, 100, 100, 105, 105, 420, 480, 18, 2, 50, 0),
	GUNNER(14, 904316, ENGINEER, 100, 105, 105, 100, 100, 100, 360, 400, 18, 2, 50, 0),
	ARTIST(15, 904317, true, 95, 95, 100, 100, 100, 105, 320, 600, 18, 2, 50, 0),
	BARD(16, 904318, ARTIST, 90, 100, 100, 100, 110, 110, 320, 520, 18, 2, 50, 50);

	/** This id is used on client side */
	private final byte classId;

	private final int nameId;

	/** This is the mask for this class id, used with bitwise AND in arguments that contain more than one possible class */
	private final int idMask;

	/** Tells whether player can create new character with this class */
	private PlayerClass startingClass;

	private Map<Integer, StatsTemplate> templatesByLevel = new HashMap<>();
	private final int power, health, agility, accuracy, knowledge, will, healthMultiplier, willMultiplier;

	PlayerClass(int classId, int nameId, PlayerClass startingClass, int power, int health, int agility, int accuracy, int knowledge, int will, int healthMultiplier, int willMultiplier, int physicalAttack, int physicalCritical, int magicalCritical, int magicalCriticalResist) {
		this(classId, nameId, false, power, health, agility, accuracy, knowledge, will, healthMultiplier, willMultiplier, physicalAttack, physicalCritical, magicalCritical, magicalCriticalResist);
		this.startingClass = startingClass;
	}

	PlayerClass(Integer classId, int nameId, boolean isStartingClass, int power, int health, int agility, int accuracy, int knowledge, int will, int healthMultiplier, int willMultiplier, int physicalAttack, int physicalCritical, int magicalCritical, int magicalCriticalResist) {
		this.nameId = nameId;
		this.classId = classId.byteValue();
		this.idMask = (int) Math.pow(2, classId);
		if (isStartingClass)
			this.startingClass = this;
		this.power = power;
		this.health = health;
		this.agility = agility;
		this.accuracy = accuracy;
		this.knowledge = knowledge;
		this.will = will;
		this.healthMultiplier = healthMultiplier;
		this.willMultiplier = willMultiplier;
		initializeTemplatesForEachLevel(physicalAttack, physicalCritical, magicalCritical, magicalCriticalResist);
	}

	private void initializeTemplatesForEachLevel(int physicalAttack, int physicalCritical, int magicalCritical, int magicalCriticalResist) {
		for (int level = 1; level <= 65; level++) {
			StatsTemplate statsTemplate = new StatsTemplate();
			statsTemplate.setPower(power);
			statsTemplate.setHealth(health);
			statsTemplate.setAgility(agility);
			statsTemplate.setBaseAccuracy(accuracy);
			statsTemplate.setKnowledge(knowledge);
			statsTemplate.setWill(will);
			statsTemplate.setMaxHp(PlayerStatCalculator.calculateMaxHp(this, level));
			statsTemplate.setMaxMp(PlayerStatCalculator.calculateMaxMp(this, level));
			statsTemplate.setBlock(PlayerStatCalculator.calculateBlockEvasionOrParry(level));
			statsTemplate.setParry(PlayerStatCalculator.calculateBlockEvasionOrParry(level));
			statsTemplate.setEvasion(PlayerStatCalculator.calculateBlockEvasionOrParry(level));
			statsTemplate.setAccuracy(PlayerStatCalculator.calculatePhysicalAccuracy(level));
			statsTemplate.setMacc(PlayerStatCalculator.calculateMagicalAccuracy(level));
			statsTemplate.setPcrit(physicalCritical);
			statsTemplate.setMcrit(magicalCritical);
			statsTemplate.setStrikeResist(PlayerStatCalculator.calculateStrikeResist(level));
			statsTemplate.setAttack(physicalAttack);
			statsTemplate.setSpellResist(magicalCriticalResist);
			CreatureSpeeds speeds = new CreatureSpeeds();
			speeds.setWalkSpeed(1.5f);
			speeds.setRunSpeed(6f);
			speeds.setFlySpeed(9f);
			statsTemplate.setSpeeds(speeds);
			templatesByLevel.put(level, statsTemplate);
		}
	}

	/**
	 * Returns client-side id for this PlayerClass
	 * 
	 * @return classID
	 */
	public byte getClassId() {
		return classId;
	}

	/**
	 * Returns <tt>PlayerClass</tt> object correlating with given classId.
	 * 
	 * @param classId
	 *          - id of player class
	 * @return PlayerClass that matches the given classId. If there isn't one, {@link IllegalArgumentException} is being thrown.
	 */
	public static PlayerClass getPlayerClassById(byte classId) {
		return getPlayerClassById(classId, false);
	}

	public static PlayerClass getPlayerClassById(byte classId, boolean ignoreInvalidClassId) {
		for (PlayerClass pc : values()) {
			if (pc.getClassId() == classId)
				return pc;
		}
		if (ignoreInvalidClassId)
			return null;
		throw new IllegalArgumentException("There is no player class with id " + classId);
	}

	public int getL10nId() {
		return nameId;
	}

	/**
	 * @return true if this is one of starting classes ( player can create char with this class )
	 */
	public boolean isStartingClass() {
		return startingClass == this;
	}

	public PlayerClass getStartingClass() {
		return startingClass;
	}

	public int getMask() {
		return idMask;
	}

	public boolean isPhysicalClass() {
		switch (this) {
			case WARRIOR:
			case GLADIATOR:
			case TEMPLAR:
			case SCOUT:
			case ASSASSIN:
			case RANGER:
			case CHANTER:
				return true;
			default:
				return false;
		}
	}

	public String getIconImage() {
		return switch (this) {
			case WARRIOR -> "textures/ui/EMBLEM/icon_emblem_warrior.dds";
			case GLADIATOR -> "textures/ui/EMBLEM/icon_emblem_fighter.dds";
			case TEMPLAR -> "textures/ui/EMBLEM/icon_emblem_knight.dds";
			case SCOUT -> "textures/ui/EMBLEM/icon_emblem_scout.dds";
			case ASSASSIN -> "textures/ui/EMBLEM/icon_emblem_assassin.dds";
			case RANGER -> "textures/ui/EMBLEM/icon_emblem_ranger.dds";
			case MAGE -> "textures/ui/EMBLEM/icon_emblem_mage.dds";
			case SORCERER -> "textures/ui/EMBLEM/icon_emblem_wizard.dds";
			case SPIRIT_MASTER -> "textures/ui/EMBLEM/icon_emblem_elementalist.dds";
			case PRIEST -> "textures/ui/EMBLEM/icon_emblem_cleric.dds"; // cleric and priest images are switched in client
			case CLERIC -> "textures/ui/EMBLEM/icon_emblem_priest.dds"; // cleric and priest images are switched in client
			case CHANTER -> "textures/ui/EMBLEM/icon_emblem_chanter.dds";
			case ENGINEER -> "textures/ui/EMBLEM/Icon_emblem_Engineer.dds";
			case RIDER -> "textures/ui/EMBLEM/Icon_emblem_Rider.dds";
			case GUNNER -> "textures/ui/EMBLEM/Icon_emblem_Gunner.dds";
			case ARTIST -> "textures/ui/EMBLEM/Icon_emblem_Artist.dds";
			case BARD -> "textures/ui/EMBLEM/Icon_emblem_Bard.dds";
		};
	}

	public StatsTemplate getStatsTemplateFor(int level) {
		StatsTemplate template = templatesByLevel.get(Math.min(level, GSConfig.PLAYER_MAX_LEVEL));
		return Objects.requireNonNull(template, () -> "Missing template for PlayerClass." + this + " on level " + level);
	}

	public int getPower() {
		return power;
	}

	public int getHealth() {
		return health;
	}

	public int getAgility() {
		return agility;
	}

	public int getAccuracy() {
		return accuracy;
	}

	public int getKnowledge() {
		return knowledge;
	}

	public int getWill() {
		return will;
	}

	public int getWillMultiplier() {
		return willMultiplier;
	}

	public int getHealthMultiplier() {
		return healthMultiplier;
	}

	public int getAgilityMultiplier() {
		return 310;
	}

	public int getAccuracyMultiplier() {
		return 200;
	}

	public int getNoWeaponPowerMultiplier() {
		return 70;
	}
}
