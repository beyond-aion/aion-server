package com.aionemu.gameserver.model.templates.item.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Adds a manastone slot to an item.
 * <p>
 * TODO logic: the retail rules are not known yet; only a test item uses it.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OptionExtendAction")
public class OptionExtendAction extends UnimplementedItemAction {
}
