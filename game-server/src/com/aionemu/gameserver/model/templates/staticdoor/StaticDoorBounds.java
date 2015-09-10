package com.aionemu.gameserver.model.templates.staticdoor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StaticDoorBounds")
public class StaticDoorBounds {

	@XmlAttribute
	private float x1;

	@XmlAttribute
	private float y1;

	@XmlAttribute
	private float z1;

	@XmlAttribute
	private float x2;

	@XmlAttribute
	private float y2;

	@XmlAttribute
	private float z2;

	@XmlTransient
	private BoundingBox boundingBox;

	public BoundingBox getBoundingBox() {
		if (boundingBox == null)
			boundingBox = new BoundingBox(new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2));
		return boundingBox;
	}
}
