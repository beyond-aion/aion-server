package com.aionemu.gameserver.model.templates.item.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Activates an account pack (Pack_1 … Pack_11, Char_Slot_8) for a number of minutes: lifts starter account restrictions and grants bonuses
 * such as AP gain, repose energy or Crucible rewards.
 * <p>
 * TODO logic: needs the pack table (bm_pack.xml of the retail server data) and starter account restrictions.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "F2pPackAction")
public class F2pPackAction extends UnimplementedItemAction {

	@XmlAttribute
	private String name;
	@XmlAttribute
	private int minutes;

	public String getName() {
		return name;
	}

	public int getMinutes() {
		return minutes;
	}
}
