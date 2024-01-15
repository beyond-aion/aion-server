package com.aionemu.gameserver.model.templates.staticdoor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.VisibleObjectTemplate;

/**
 * @author Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StaticDoor")
public class StaticDoorTemplate extends VisibleObjectTemplate {

	@XmlAttribute
	private int id;
	@XmlAttribute(name = "keyid")
	private int keyId;
	@XmlAttribute
	private float x;
	@XmlAttribute
	private float y;
	@XmlAttribute
	private float z;
	@XmlAttribute(name = "state")
	private int state;

	public int getId() {
		return id;
	}

	public int getKeyId() {
		return keyId;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public int getState() {
		return state;
	}

	@Override
	public int getTemplateId() {
		return 300001;
	}

	@Override
	public String getName() {
		return "door";
	}

	@Override
	public int getL10nId() {
		return 0;
	}
}