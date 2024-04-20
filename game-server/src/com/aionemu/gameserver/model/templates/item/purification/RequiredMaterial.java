package com.aionemu.gameserver.model.templates.item.purification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ranastic, Navyan, Estrayl
 */
@XmlRootElement(name = "RequiredMaterial")
@XmlAccessorType(XmlAccessType.FIELD)
public class RequiredMaterial {

	@XmlAttribute(name = "item_id")
	private int itemId;
	@XmlAttribute(name = "item_count")
	private int itemCount;

	public int getItemId() {
		return itemId;
	}

	public int getItemCount() {
		return itemCount;
	}
}
