package com.aionemu.gameserver.model.templates.flypath;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.geometry.Point3D;

/**
 * @author KID
 */
@XmlRootElement(name = "flypath_location")
@XmlAccessorType(XmlAccessType.NONE)
public class FlyPathEntry {

	@XmlAttribute(name = "id", required = true)
	private int id;

	@XmlAttribute(name = "sx", required = true)
	private float startX;
	@XmlAttribute(name = "sy", required = true)
	private float startY;
	@XmlAttribute(name = "sz", required = true)
	private float startZ;
	@XmlAttribute(name = "sworld", required = true)
	private int sworld;

	@XmlAttribute(name = "ex", required = true)
	private float endX;
	@XmlAttribute(name = "ey", required = true)
	private float endY;
	@XmlAttribute(name = "ez", required = true)
	private float endZ;
	@XmlAttribute(name = "eworld", required = true)
	private int eworld;

	@XmlAttribute(name = "time", required = true)
	private float time;

	/**
	 * Points of the windstream path, one per second of flight. Only set for windstreams.
	 */
	@XmlElement(name = "windstream_point")
	private List<WindstreamPoint> windstreamPoints;

	public int getId() {
		return id;
	}

	public float getStartX() {
		return startX;
	}

	public float getStartY() {
		return startY;
	}

	public float getStartZ() {
		return startZ;
	}

	public float getEndX() {
		return endX;
	}

	public float getEndY() {
		return endY;
	}

	public float getEndZ() {
		return endZ;
	}

	public int getStartWorldId() {
		return sworld;
	}

	public int getEndWorldId() {
		return eworld;
	}

	public int getTimeInMs() {
		return (int) (time * 1000);
	}

	public boolean isWindstream() {
		return windstreamPoints != null;
	}

	/**
	 * @param progress
	 *          flight progress in milliseconds, as sent by the client
	 * @return The interpolated position on the windstream path, or the end point if the progress exceeds the path.
	 */
	public Point3D getWindstreamPosition(int progress) {
		int index = Math.max(0, progress) / 1000;
		if (index >= windstreamPoints.size() - 1) {
			WindstreamPoint end = windstreamPoints.getLast();
			return new Point3D(end.getX(), end.getY(), end.getZ());
		}
		float ratio = (Math.max(0, progress) % 1000) / 1000f;
		WindstreamPoint from = windstreamPoints.get(index), to = windstreamPoints.get(index + 1);
		return new Point3D(from.getX() + (to.getX() - from.getX()) * ratio, from.getY() + (to.getY() - from.getY()) * ratio,
			from.getZ() + (to.getZ() - from.getZ()) * ratio);
	}

	/**
	 * @return True if the path may be entered at the given progress (in milliseconds), which is the case if the point of this or the next second
	 *         allows it, or the progress is within the last second of the path.
	 */
	public boolean canEnterWindstream(int progress) {
		int index = Math.max(0, progress) / 1000;
		return index + 1 >= windstreamPoints.size() || windstreamPoints.get(index).canEnter() || windstreamPoints.get(index + 1).canEnter();
	}

	/**
	 * @return True if the path may be left at the given progress (in milliseconds), which is the case if the point of this or the next second
	 *         allows it, or the progress is within the last second of the path.
	 */
	public boolean canLeaveWindstream(int progress) {
		int index = Math.max(0, progress) / 1000;
		return index + 1 >= windstreamPoints.size() || windstreamPoints.get(index).canExit() || windstreamPoints.get(index + 1).canExit();
	}
}
