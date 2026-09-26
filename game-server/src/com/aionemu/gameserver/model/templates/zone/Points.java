package com.aionemu.gameserver.model.templates.zone;

import java.util.List;

import javax.xml.bind.annotation.*;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Points")
public class Points {

	@XmlElement(name = "point", required = true)
	protected List<Point2D> points;
	@XmlAttribute(name = "top")
	protected float top;
	@XmlAttribute(name = "bottom")
	protected float bottom;

	public List<Point2D> getPoints() {
		return points;
	}

	public float getTop() {
		return top;
	}

	public float getBottom() {
		return bottom;
	}

}
