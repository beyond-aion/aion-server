package com.aionemu.gameserver.skillengine.model;

import java.util.HashMap;
import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "motion_time")
public class MotionTime {

	@XmlElement(name = "asmodian_female")
	private List<Times> asmodianFemale;
	@XmlElement(name = "asmodian_male")
	private List<Times> asmodianMale;
	@XmlElement(name = "elyos_female")
	private List<Times> elyosFemale;
	@XmlElement(name = "elyos_male")
	private List<Times> elyosMale;
	@XmlElement(name = "robot")
	private List<Times> robot;
	@XmlElement(name = "npc")
	private List<NpcMotionTime> npc;

	@XmlAttribute(required = true)
	private String name;

	public String getName() {
		return name;
	}

	@XmlTransient
	private HashMap<WeaponTypeWrapper, HashMap<Integer, Times>> asmodianFemaleTimeForWeaponType = new HashMap<>();

	@XmlTransient
	private HashMap<WeaponTypeWrapper, HashMap<Integer, Times>> asmodianMaleTimeForWeaponType = new HashMap<>();

	@XmlTransient
	private HashMap<WeaponTypeWrapper, HashMap<Integer, Times>> elyosFemaleTimeForWeaponType = new HashMap<>();

	@XmlTransient
	private HashMap<WeaponTypeWrapper, HashMap<Integer, Times>> elyosMaleTimeForWeaponType = new HashMap<>();

	@XmlTransient
	HashMap<Integer, Times> robotTimes = new HashMap<>();

	@XmlTransient
	private final HashMap<Integer, HashMap<Integer, Times>> npcTimes = new HashMap<>();

	public Times getTimesForNpc(int npcId, int id) {
		HashMap<Integer, Times> times = npcTimes.get(npcId);
		if (times != null) {
			for (int i = id; i > 0; i--) {
				if (times.get(i) != null)
					return times.get(i);
			}
		}
		return null;
	}

	public Times getTimesFor(Creature creature, int id) {
		return creature instanceof Player player ? getTimesFor(player, id) : getTimesForNpc(creature.getTransformModel().getModelId(), id);
	}

	public Times getTimesFor(Player player, int id) {
		if (player.isTransformed()) {
			Times times = getTimesForNpc(player.getTransformModel().getModelId(), id);
			if (times != null)
				return times;
		}
		WeaponTypeWrapper weapons = player.isInRobotMode() ? null : new WeaponTypeWrapper(player.getEquipment().getMainHandWeaponType(), player.getEquipment().getOffHandWeaponType());
		for (int i = id; i > 0; i--) {
			if (player.isInRobotMode()) {
				if (robotTimes.get(i) != null) {
					return robotTimes.get(i);
				}
			} else {
				HashMap<Integer, Times> times = null;
				switch (player.getRace()) {
					case ASMODIANS:
						if (player.getGender() == Gender.FEMALE) {
							times = asmodianFemaleTimeForWeaponType.get(weapons);

						} else {
							times = asmodianMaleTimeForWeaponType.get(weapons);
						}
						break;
					case ELYOS:
						if (player.getGender() == Gender.FEMALE) {
							times = elyosFemaleTimeForWeaponType.get(weapons);

						} else {
							times = elyosMaleTimeForWeaponType.get(weapons);
						}
						break;
				}
				if (times != null && times.get(i) != null) {
					return times.get(i);
				}
			}
		}
		return null;
	}

	void afterUnmarshal(Unmarshaller u, Object parent) {
		parseTimesFrom(asmodianFemale, asmodianFemaleTimeForWeaponType);
		parseTimesFrom(asmodianMale, asmodianMaleTimeForWeaponType);
		parseTimesFrom(elyosFemale, elyosFemaleTimeForWeaponType);
		parseTimesFrom(elyosMale, elyosMaleTimeForWeaponType);
		if (npc != null) {
			for (NpcMotionTime time : npc) {
				for (int npcId : time.getNpcIds()) {
					if (npcTimes.computeIfAbsent(npcId, k -> new HashMap<>()).putIfAbsent(time.getId(), time) != null)
						throw new IllegalArgumentException("Duplicate NPC motion timing: " + name + " / " + npcId + " / " + time.getId());
				}
			}
		}
		if (robot != null) {
			for (Times time : robot) {
				robotTimes.put(time.getId(), time);
			}
		}

		asmodianFemale = null;
		asmodianMale = null;
		elyosFemale = null;
		elyosMale = null;
		robot = null;
		npc = null;
	}

	private void parseTimesFrom(List<Times> times, HashMap<WeaponTypeWrapper, HashMap<Integer, Times>> map) {
		if (times == null) {
			return;
		}
		for (Times t : times) {
			WeaponTypeWrapper wrapper;
			switch (t.getWeapon()) {
				case "1hand":
					wrapper = new WeaponTypeWrapper(ItemGroup.SWORD, null);
					break;
				case "2hand":
					wrapper = new WeaponTypeWrapper(ItemGroup.GREATSWORD, null);
					break;
				case "keyblade":
					wrapper = new WeaponTypeWrapper(ItemGroup.KEYBLADE, null);
					break;
				case "polearm":
					wrapper = new WeaponTypeWrapper(ItemGroup.POLEARM, null);
					break;
				case "dagger":
					wrapper = new WeaponTypeWrapper(ItemGroup.DAGGER, null);
					break;
				case "mace":
					wrapper = new WeaponTypeWrapper(ItemGroup.MACE, null);
					break;
				case "staff":
					wrapper = new WeaponTypeWrapper(ItemGroup.STAFF, null);
					break;
				case "2weapon":
					wrapper = new WeaponTypeWrapper(ItemGroup.DAGGER, ItemGroup.DAGGER);
					map.computeIfAbsent(new WeaponTypeWrapper(ItemGroup.SWORD, ItemGroup.SWORD), k -> new HashMap<>()).put(t.getId(), t);
					map.computeIfAbsent(new WeaponTypeWrapper(ItemGroup.MACE, ItemGroup.MACE), k -> new HashMap<>()).put(t.getId(), t);
					// other combinations don't need to be added, as the WeaponTypeWrapper constructor already limits them
					break;
				case "noweapon":
					wrapper = new WeaponTypeWrapper(null, null);
					break;
				case "book":
					wrapper = new WeaponTypeWrapper(ItemGroup.SPELLBOOK, null);
					break;
				case "orb":
					wrapper = new WeaponTypeWrapper(ItemGroup.ORB, null);
					break;
				case "1gun":
					wrapper = new WeaponTypeWrapper(ItemGroup.GUN, null);
					break;
				case "2gun":
					wrapper = new WeaponTypeWrapper(ItemGroup.GUN, ItemGroup.GUN);
					break;
				case "cannon":
					wrapper = new WeaponTypeWrapper(ItemGroup.CANNON, null);
					break;
				case "bow":
					wrapper = new WeaponTypeWrapper(ItemGroup.BOW, null);
					break;
				case "harp":
					wrapper = new WeaponTypeWrapper(ItemGroup.HARP, null);
					break;
				default:
					throw new IllegalArgumentException(t.getWeapon() + " is not implemented");
			}
			map.computeIfAbsent(wrapper, k -> new HashMap<>()).put(t.getId(), t);
		}
	}
}
