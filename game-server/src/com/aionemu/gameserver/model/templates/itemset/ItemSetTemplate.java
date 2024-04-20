package com.aionemu.gameserver.model.templates.itemset;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.stats.calc.StatOwner;

/**
 * @author ATracer, Antivirus
 */
@XmlRootElement(name = "itemset")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemSetTemplate implements StatOwner {

	@XmlElement(required = true)
	protected List<ItemPart> itempart;
	@XmlElement(required = true)
	protected List<PartBonus> partbonus;
	protected FullBonus fullbonus;
	@XmlAttribute
	protected String name;
	@XmlAttribute
	protected int id;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (fullbonus != null) {
			// Set number of items to apply the full bonus
			fullbonus.setNumberOfItems(itempart.size());
		}
	}

	public List<ItemPart> getItempart() {
		return itempart;
	}

	public List<PartBonus> getPartbonus() {
		return partbonus;
	}

	public FullBonus getFullbonus() {
		return fullbonus;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

}
