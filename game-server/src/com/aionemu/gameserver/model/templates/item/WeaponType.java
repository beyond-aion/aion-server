package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlType(name = "weapon_type")
@XmlEnum
public enum WeaponType {
	DAGGER_1H(new int[] { 30, 9 }, 1),
	MACE_1H(new int[] { 3, 10 }, 1),
	SWORD_1H(new int[] { 1, 8 }, 1),
	TOOLHOE_1H(new int[] {}, 1),
	GUN_1H(new int[] { 83, 76 }, 1),
	BOOK_2H(new int[] { 64 }, 2),
	ORB_2H(new int[] { 64 }, 2),
	POLEARM_2H(new int[] { 16 }, 2),
	STAFF_2H(new int[] { 53 }, 2),
	SWORD_2H(new int[] { 15 }, 2),
	TOOLPICK_2H(new int[] {}, 2),
	TOOLROD_2H(new int[] {}, 2),
	BOW(new int[] { 17 }, 2),
	CANNON_2H(new int[] { 77 }, 2),
	HARP_2H(new int[] { 92, 78 }, 2),
	GUN_2H(new int[] {}, 2),
	KEYBLADE_2H(new int[] { 76, 79 }, 2),
	KEYHAMMER_2H(new int[] {}, 2);

	private int[] requiredSkill;
	private int slots;

	private WeaponType(int[] requiredSkills, int slots) {
		this.requiredSkill = requiredSkills;
		this.slots = slots;
	}

	public int[] getRequiredSkills() {
		return requiredSkill;
	}

	public int getRequiredSlots() {
		return slots;
	}

	/**
	 * @return int
	 */
	public int getMask() {
		return 1 << this.ordinal();
	}
}
