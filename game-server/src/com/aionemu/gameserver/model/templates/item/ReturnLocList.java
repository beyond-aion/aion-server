package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ginho1
 */
@XmlType(name = "ReturnLocList")
public class ReturnLocList extends ResultedItemsCollection {

	@XmlAttribute(name = "index")
	protected int index;
	@XmlAttribute(name = "worldid")
	protected int worldid;
	@XmlAttribute(name = "desc")
	protected String desc;
	@XmlAttribute(name = "alias")
	protected String alias;

	public final float getIndex() {
		return index;
	}

	public final int getWorldid() {
		return worldid;
	}

	public final String getDesc() {
		return desc;
	}

	public final String getAlias() {
		return alias;
	}
}
