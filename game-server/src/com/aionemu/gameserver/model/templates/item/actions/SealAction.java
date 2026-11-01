package com.aionemu.gameserver.model.templates.item.actions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Seals an item: a sealed item cannot be enhanced, modified, extracted, traded or soulbound until it is unsealed.
 * <p>
 * TODO logic: needs the item seal state.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SealAction")
public class SealAction extends UnimplementedItemAction {
}
