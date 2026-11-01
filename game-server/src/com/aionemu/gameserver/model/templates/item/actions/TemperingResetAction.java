package com.aionemu.gameserver.model.templates.item.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Resets the tempering of an item.
 * <p>
 * TODO logic: the retail rules (which items, what is kept) are not known yet; only test items use it.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TemperingResetAction")
public class TemperingResetAction extends UnimplementedItemAction {
}
