package com.aionemu.gameserver.model.templates.quest;

import java.util.List;

import jakarta.xml.bind.annotation.*;

/**
 * @author MrPoke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CollectItems", propOrder = { "collectItem" })
public class CollectItems {

	@XmlElement(name = "collect_item")
	protected List<CollectItem> collectItem;

	@XmlAttribute(name = "start_check")
	protected boolean startCheck;

	public List<CollectItem> getCollectItem() {
		return collectItem == null ? List.of() : collectItem;
	}

	public boolean getStartCheck() {
		return startCheck;
	}

}
