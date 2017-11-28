package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;
import com.aionemu.gameserver.model.templates.item.ItemQuality;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractHouseObject")
@XmlSeeAlso({ PlaceableHouseObject.class })
public abstract class AbstractHouseObject extends VisibleObjectTemplate {

	@XmlAttribute(name = "talking_distance", required = true)
	protected float talkingDistance;

	@XmlAttribute(required = true)
	protected ItemQuality quality;

	@XmlAttribute(required = true)
	protected HousingCategory category;

	@XmlAttribute(name = "name_id", required = true)
	protected int nameId;

	@XmlAttribute(required = true)
	protected int id;

	@XmlAttribute(name = "can_dye")
	protected boolean canDye;

	@Override
	public int getTemplateId() {
		return id;
	}

	public float getTalkingDistance() {
		return talkingDistance;
	}

	public ItemQuality getQuality() {
		return quality;
	}

	public HousingCategory getCategory() {
		return category;
	}

	public boolean getCanDye() {
		return canDye;
	}

	@Override
	public int getL10nId() {
		return nameId;
	}

	@Override
	public String getName() {
		return null;
	}

}
