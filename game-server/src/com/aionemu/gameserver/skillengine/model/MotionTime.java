package com.aionemu.gameserver.skillengine.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.items.NpcEquippedGear;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;

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
	private List<NpcTimes> npc;

	@XmlAttribute(required = true)
	private String name;

	/** Every weapon setup that holds one weapon, or none, as the animation tokens map them */
	private static final List<WeaponTypeWrapper> SINGLE_WEAPONS = List.of(new WeaponTypeWrapper(ItemGroup.SWORD, null),
		new WeaponTypeWrapper(ItemGroup.TOOLHOES, null), new WeaponTypeWrapper(ItemGroup.GREATSWORD, null), new WeaponTypeWrapper(ItemGroup.TOOLPICKS, null),
		new WeaponTypeWrapper(ItemGroup.TOOLRODS, null), new WeaponTypeWrapper(ItemGroup.KEYBLADE, null), new WeaponTypeWrapper(ItemGroup.POLEARM, null),
		new WeaponTypeWrapper(ItemGroup.DAGGER, null), new WeaponTypeWrapper(ItemGroup.MACE, null), new WeaponTypeWrapper(ItemGroup.STAFF, null),
		new WeaponTypeWrapper(null, null), new WeaponTypeWrapper(ItemGroup.SPELLBOOK, null), new WeaponTypeWrapper(ItemGroup.ORB, null),
		new WeaponTypeWrapper(ItemGroup.GUN, null), new WeaponTypeWrapper(ItemGroup.GUN, ItemGroup.GUN), new WeaponTypeWrapper(ItemGroup.CANNON, null),
		new WeaponTypeWrapper(ItemGroup.BOW, null), new WeaponTypeWrapper(ItemGroup.HARP, null));

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

	/** Times of weapons without an animation of their own, borrowed from the slowest weapon that has one */
	@XmlTransient
	private final Set<HashMap<Integer, Times>> inheritedTimes = Collections.newSetFromMap(new IdentityHashMap<>());

	/** Hit times per variant on an NPC model, keyed by {@link #npcKey} */
	@XmlTransient
	private final HashMap<String, int[]> npcHitTimes = new HashMap<>();

	@XmlTransient
	private final HashMap<String, Integer> npcAnimationLengths = new HashMap<>();

	/**
	 * @return Milliseconds from the start of this motion to its hit point on the model of the given NPC for the weapon it holds, one entry per
	 *         animation variant, or null if its model has no such animation
	 */
	public int[] getNpcHitTimesMillis(NpcTemplate npc) {
		return npc.getMesh() == null ? null : npcHitTimes.get(npcKey(npc.getMesh(), getNpcAnimationWeapon(npc)));
	}

	/**
	 * @return Length of this motion's animation on the model of the given NPC for the weapon it holds in milliseconds, or null if its model has no
	 *         such animation or the length is not given
	 */
	public Integer getNpcAnimationLengthMillis(NpcTemplate npc) {
		return npc.getMesh() == null ? null : npcAnimationLengths.get(npcKey(npc.getMesh(), getNpcAnimationWeapon(npc)));
	}

	private static String npcKey(String mesh, String weapon) {
		return mesh + '/' + weapon;
	}

	/**
	 * @return The weapon token of the NPC's animation names, picked from the items in the main and the off hand: a weapon in both hands plays the
	 *         dual animations ({@code 2gun} with a gun in the main hand), otherwise the main hand weapon decides, and an empty main hand plays
	 *         {@code noweapon} even when the off hand holds a weapon
	 */
	public static String getNpcAnimationWeapon(NpcTemplate npc) {
		NpcEquippedGear equipment = npc.getEquipment();
		ItemTemplate mainHand = equipment == null ? null : equipment.getItem(ItemSlot.MAIN_HAND);
		ItemTemplate offHand = equipment == null ? null : equipment.getItem(ItemSlot.SUB_HAND);
		String token = mainHand == null || !mainHand.isWeapon() ? null : switch (mainHand.getItemGroup()) {
			case SWORD, TOOLHOES -> "1hand";
			case DAGGER -> "dagger";
			case MACE -> "mace";
			case GREATSWORD, TOOLPICKS, TOOLRODS -> "2hand";
			case POLEARM -> "polearm";
			case STAFF -> "staff";
			case KEYBLADE -> "keyblade";
			case HARP -> "harp";
			case ORB -> "orb";
			case SPELLBOOK -> "book";
			case GUN -> "1gun";
			case CANNON -> "cannon";
			case BOW -> "bow";
			default -> null;
		};
		if (token == null)
			return "noweapon";
		if (offHand != null && offHand.isWeapon())
			return mainHand.getItemGroup() == ItemGroup.GUN ? "2gun" : "2weapon";
		return token;
	}

	/**
	 * @return False if the player's weapon has no animation of its own for this motion, so its times are borrowed from the slowest weapon that has one.
	 *         The client then plays nothing and reports a hit time without any animation time in it.
	 */
	public boolean hasOwnAnimation(Player player) {
		if (player.isInRobotMode())
			return true;
		HashMap<Integer, Times> times = getTimesForWeapons(player);
		return times == null || !inheritedTimes.contains(times);
	}

	private HashMap<Integer, Times> getTimesForWeapons(Player player) {
		WeaponTypeWrapper weapons = new WeaponTypeWrapper(player.getEquipment().getMainHandWeaponType(), player.getEquipment().getOffHandWeaponType());
		return switch (player.getRace()) {
			case ASMODIANS -> (player.getGender() == Gender.FEMALE ? asmodianFemaleTimeForWeaponType : asmodianMaleTimeForWeaponType).get(weapons);
			case ELYOS -> (player.getGender() == Gender.FEMALE ? elyosFemaleTimeForWeaponType : elyosMaleTimeForWeaponType).get(weapons);
			default -> null;
		};
	}

	public Times getTimesFor(Player player, int id) {
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
				if (times != null) {
					Times time = times.get(i);
					if (time != null)
						return time;
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
		inheritMissingWeapons(asmodianFemaleTimeForWeaponType);
		inheritMissingWeapons(asmodianMaleTimeForWeaponType);
		inheritMissingWeapons(elyosFemaleTimeForWeaponType);
		inheritMissingWeapons(elyosMaleTimeForWeaponType);
		if (robot != null) {
			for (Times time : robot) {
				robotTimes.put(time.getId(), time);
			}
		}
		if (npc != null) {
			for (NpcTimes times : npc) {
				String key = npcKey(times.getMesh(), times.getWeapon());
				if (times.getHitTimesMillis().isEmpty())
					throw new IllegalArgumentException("Motion " + name + " has no hit time for " + key);
				if (npcHitTimes.put(key, times.getHitTimesMillis().stream().mapToInt(Integer::intValue).toArray()) != null)
					throw new IllegalArgumentException("Motion " + name + " has more than one entry for " + key);
				if (times.getLengthMillis() != null)
					npcAnimationLengths.put(key, times.getLengthMillis());
			}
		}

		asmodianFemale = null;
		asmodianMale = null;
		elyosFemale = null;
		elyosMale = null;
		robot = null;
		npc = null;
	}

	/**
	 * Gives every weapon without an animation of its own the slowest one of the weapons that have it, so that no weapon slips past the animation
	 * checks. Dual wielding is left alone, a motion without it falls back to the default hit time.
	 */
	private void inheritMissingWeapons(HashMap<WeaponTypeWrapper, HashMap<Integer, Times>> map) {
		HashMap<Integer, Times> slowest = new HashMap<>();
		for (WeaponTypeWrapper weapon : SINGLE_WEAPONS) {
			HashMap<Integer, Times> times = map.get(weapon);
			if (times != null)
				times.forEach((id, t) -> slowest.merge(id, t, (a, b) -> b.getMinTime() > a.getMinTime() ? b : a));
		}
		if (slowest.isEmpty())
			return;
		for (WeaponTypeWrapper weapon : SINGLE_WEAPONS)
			map.computeIfAbsent(weapon, k -> {
				HashMap<Integer, Times> borrowed = new HashMap<>(slowest);
				inheritedTimes.add(borrowed);
				return borrowed;
			});
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
					map.computeIfAbsent(new WeaponTypeWrapper(ItemGroup.TOOLHOES, null), k -> new HashMap<>()).put(t.getId(), t);
					break;
				case "2hand":
					wrapper = new WeaponTypeWrapper(ItemGroup.GREATSWORD, null);
					map.computeIfAbsent(new WeaponTypeWrapper(ItemGroup.TOOLPICKS, null), k -> new HashMap<>()).put(t.getId(), t);
					map.computeIfAbsent(new WeaponTypeWrapper(ItemGroup.TOOLRODS, null), k -> new HashMap<>()).put(t.getId(), t);
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
