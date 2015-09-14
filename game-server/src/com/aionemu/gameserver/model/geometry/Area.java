package com.aionemu.gameserver.model.geometry;

import com.aionemu.gameserver.model.templates.zone.Point2D;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * Basic interface for all areas in AionEmu.<br>
 * It should be implemented in different ways for performance reasons.<br>
 * For instance, we don't need complex math for squares or circles, but we need it for more complex polygons.
 * 
 * @author SoulKeeper
 */
public interface Area {

	/**
	 * Returns true if point is inside area ignoring z value
	 * 
	 * @param point
	 *          point to check
	 * @return point is inside or not
	 */
	public boolean isInside2D(Point2D point);

	/**
	 * Returns true if coords are inside area ignoring z value
	 * 
	 * @param x
	 *          x coord
	 * @param y
	 *          y coord
	 * @return coords are inside or not
	 */
	public boolean isInside2D(float x, float y);

	/**
	 * Returns true if point is inside area
	 * 
	 * @param point
	 *          point to check
	 * @return true if point is inside
	 */
	public boolean isInside3D(Point3D point);

	/**
	 * Returns true if coors are inside area
	 * 
	 * @param x
	 *          x coord
	 * @param y
	 *          y coord
	 * @param z
	 *          z coord
	 * @return true if coords are inside
	 */
	public boolean isInside3D(float x, float y, float z);

	/**
	 * Checks if z coord is insize
	 * 
	 * @param point
	 *          point to check
	 * @return is z inside or not
	 */
	public boolean isInsideZ(Point3D point);

	/**
	 * Checks is z coord is inside
	 * 
	 * @param z
	 *          z coord
	 * @return is z inside or not
	 */
	public boolean isInsideZ(float z);

	/**
	 * Returns distance from point to closest point of this area ignoring z.<br>
	 * Returns 0 if point is inside area.
	 * 
	 * @param point
	 *          point to calculate distance from
	 * @return distance or 0 if is inside area
	 */
	public double getDistance2D(Point2D point);

	/**
	 * Returns distance from point to closest point of this area ignoring z.<br>
	 * Returns 0 point is inside area.
	 * 
	 * @param x
	 *          x coord
	 * @param y
	 *          y coord
	 * @return distance or 0 if is inside area
	 */
	public double getDistance2D(float x, float y);

	/**
	 * Returns distance from point to this area.<br>
	 * Returns 0 if is inside.
	 * 
	 * @param point
	 *          point to check
	 * @return distance or 0 if is inside
	 */
	public double getDistance3D(Point3D point);

	/**
	 * Returns distance from coords to this area
	 * 
	 * @param x
	 *          x coord
	 * @param y
	 *          y coord
	 * @param z
	 *          z coord
	 * @return distance or 0 if is inside
	 */
	public double getDistance3D(float x, float y, float z);

	/**
	 * Returns closest point of area to given point.<br>
	 * Returns point with coords = point arg if is inside
	 * 
	 * @param point
	 *          point to check
	 * @return closest point
	 */
	public Point2D getClosestPoint(Point2D point);

	/**
	 * Returns closest point of area to given coords.<br>
	 * Returns point with coords x and y if coords are inside
	 * 
	 * @param x
	 *          x coord
	 * @param y
	 *          y coord
	 * @return closest point
	 */
	public Point2D getClosestPoint(float x, float y);

	/**
	 * Returns closest point of area to given point.<br>
	 * Works exactly like {@link #getClosestPoint(int, int)} if {@link #isInsideZ(int)} returns true.<br>
	 * In other case closest z edge is set as z coord.
	 * 
	 * @param point
	 *          point to check
	 * @return closest point of area to point
	 */
	public Point3D getClosestPoint(Point3D point);

	/**
	 * Returns closest point of area to given coords.<br>
	 * Works exactly like {@link #getClosestPoint(int, int)} if {@link #isInsideZ(int)} returns true.<br>
	 * In other case closest z edge is set as z coord.
	 * 
	 * @param x
	 *          x coord
	 * @param y
	 *          y coord
	 * @param z
	 *          z coord
	 * @return closest point of area to point
	 */
	public Point3D getClosestPoint(float x, float y, float z);

	/**
	 * Return minimal z of this area
	 * 
	 * @return minimal z of this area
	 */
	public float getMinZ();

	/**
	 * Returns maximal z of this area
	 * 
	 * @return maximal z of this area
	 */
	public float getMaxZ();

	public boolean intersectsRectangle(RectangleArea area);

	public int getWorldId();

	public ZoneName getZoneName();
}
