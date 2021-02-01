package com.aionemu.gameserver.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlEnum;

import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.model.templates.L10n;

/**
 * This enum represent class that a player may belong to.
 * 
 * @author Luno
 */
@XmlEnum
public enum PlayerClass implements L10n {
	WARRIOR(0, 240000, true),
	GLADIATOR(1, 240001, WARRIOR), // fighter
	TEMPLAR(2, 240002, WARRIOR), // knight
	SCOUT(3, 240003, true),
	ASSASSIN(4, 240004, SCOUT),
	RANGER(5, 240005, SCOUT),
	MAGE(6, 240006, true),
	SORCERER(7, 240007, MAGE), // wizard
	SPIRIT_MASTER(8, 240008, MAGE), // elementalist
	PRIEST(9, 240009, true),
	CLERIC(10, 240010, PRIEST),
	CHANTER(11, 240011, PRIEST),
	ENGINEER(12, 904314, true),
	RIDER(13, 904315, ENGINEER),
	GUNNER(14, 904316, ENGINEER),
	ARTIST(15, 904317, true),
	BARD(16, 904318, ARTIST);

	/** This id is used on client side */
	private final byte classId;

	private final int nameId;

	/** This is the mask for this class id, used with bitwise AND in arguments that contain more than one possible class */
	private final int idMask;

	/** Tells whether player can create new character with this class */
	private PlayerClass startingClass;

	PlayerClass(int classId, int nameId, PlayerClass startingClass) {
		this(classId, nameId, false);
		this.startingClass = startingClass;
	}

	PlayerClass(Integer classId, int nameId, boolean isStartingClass) {
		this.nameId = nameId;
		this.classId = classId.byteValue();
		this.idMask = (int) Math.pow(2, classId);
		if (isStartingClass)
			this.startingClass = this;
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
}
