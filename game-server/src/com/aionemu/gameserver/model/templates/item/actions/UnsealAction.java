package com.aionemu.gameserver.model.templates.item.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Starts removing the seal of an item; the item stays sealed until the unsealing time has passed. Special scrolls remove the special seal.
 * <p>
 * TODO logic: needs the item seal state.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnsealAction")
public class UnsealAction extends UnimplementedItemAction {

	@XmlAttribute
	private boolean special;

	public boolean isSpecial() {
		return special;
	}
}
