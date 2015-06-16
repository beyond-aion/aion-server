package com.aionemu.gameserver.skillengine.model;

import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import java.util.HashMap;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * @author kecims
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Times")
public class Times {
	
	@XmlAttribute(required = true)
	protected String times;
	
	@XmlTransient
	private HashMap<WeaponTypeWrapper, Integer> timeForWeaponType = new HashMap<>();
	
	public String getTimes() {
		return times;
	}
	
	/**
	 * @param times the times to set
	 */
	public void setTimes(String times) {
		this.times = times;
	}

	public int getTimeForWeapon(WeaponTypeWrapper weapon) {
			return timeForWeaponType.get(weapon);
	}

	void afterUnmarshal(Unmarshaller u, Object parent) {
		String[] tokens = times.split(",");
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.SPELLBOOK, null), Integer.parseInt(tokens[0]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.BOW, null), Integer.parseInt(tokens[1]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.DAGGER, null), Integer.parseInt(tokens[2]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.MACE, null), Integer.parseInt(tokens[3]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.ORB, null), Integer.parseInt(tokens[4]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.POLEARM, null), Integer.parseInt(tokens[5]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.STAFF, null), Integer.parseInt(tokens[6]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.SWORD, null), Integer.parseInt(tokens[7]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.GREATSWORD, null), Integer.parseInt(tokens[8]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.SWORD, ItemGroup.SWORD), Integer.parseInt(tokens[9]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.DAGGER, ItemGroup.DAGGER), Integer.parseInt(tokens[10]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.GUN, null), Integer.parseInt(tokens[11]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.GUN, ItemGroup.GUN), Integer.parseInt(tokens[12]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.TOOLHOES, null), Integer.parseInt(tokens[13]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.TOOLHOES, ItemGroup.TOOLHOES), Integer.parseInt(tokens[14]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.TOOLPICKS, null), Integer.parseInt(tokens[15]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.TOOLRODS, null), Integer.parseInt(tokens[16]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.CANNON, null), Integer.parseInt(tokens[17]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.HARP, null), Integer.parseInt(tokens[18]));
		timeForWeaponType.put(new WeaponTypeWrapper(ItemGroup.KEYBLADE, null), Integer.parseInt(tokens[19]));
	}
}
