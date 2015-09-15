package com.aionemu.gameserver.utils;

import java.awt.Point;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.controllers.movement.NpcMoveController;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.templates.zone.Point2D;
import com.aionemu.gameserver.skillengine.properties.AreaDirections;

/**
 * Class with basic math.<br>
 * Thanks to: <li>
 * <ul>
 * http://geom-java.sourceforge.net/
 * </ul>
 * <ul>
 * http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/DistancePoint.java
 * </ul>
 * </li> <br>
 * <br>
 * Few words about speed:
 * 
 * <pre>
 * Math.hypot(dx, dy); // Extremely slow
 * Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)); // 20 times faster than hypot
 * Math.sqrt(dx * dx + dy * dy); // 10 times faster then previous line
 * </pre>
 * 
 * We don't need squared distances for calculations, {@linkplain Math#sqrt(double)} is very fast.<br>
 * In fact the difference is very small, so it can be ignored.<br>
 * Feel free to run the following test (or to find a mistake in it ^^).<br>
 * 
 * <pre>
 * import java.util.Random;
 * 
 * public class MathSpeedTest {
 * 
 * 	private static long time;
 * 
 * 	private static long n = 100000000L;
 * 
 * 	public static void main(String[] args) {
 * 
 * 		Random r = new Random();
 * 
 * 		long x;
 * 		long y;
 * 
 * 		double res = 0;
 * 		setTime();
 * 		for (int i = 0; i &lt; n; i++) {
 * 			x = r.nextInt();
 * 			y = r.nextInt();
 * 			res = Math.sqrt(x * x + y * y);
 * 		}
 * 		printTime();
 * 		System.out.println(res);
 * 
 * 		setTime();
 * 		for (int i = 0; i &lt; n; i++) {
 * 			x = r.nextInt();
 * 			y = r.nextInt();
 * 			res = x * x + y * y;
 * 		}
 * 		printTime();
 * 		System.out.println(Math.sqrt(res));
 * 	}
 * 
 * 	public static void setTime() {
 * 		time = System.currentTimeMillis();
 * 	}
 * 
 * 	public static void printTime() {
 * 		System.out.println(System.currentTimeMillis() - time);
 * 	}
 * }
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * </pre>
 * 
 * @author Disturbing
 * @author SoulKeeper modified by Wakizashi
 */
public class MathUtil {

	/**
	 * Returns distance between two 2D points
	 * 
	 * @param point1
	 *          first point
	 * @param point2
	 *          second point
	 * @return distance between points
	 */
	public static double getDistance(Point2D point1, Point2D point2) {
		return getDistance(point1.getX(), point1.getY(), point2.getX(), point2.getY());
	}

	/**
	 * Returns distance between two sets of coords
	 * 
	 * @param x1
	 *          first x coord
	 * @param y1
	 *          first y coord
	 * @param x2
	 *          second x coord
	 * @param y2
	 *          second y coord
	 * @return distance between sets of coords
	 */
	public static double getDistance(float x1, float y1, float x2, float y2) {
		// using long to avoid possible overflows when multiplying
		float dx = x2 - x1;
		float dy = y2 - y1;

		// return Math.hypot(x2 - x1, y2 - y1); // Extremely slow
		// return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)); // 20 times faster than hypot
		return Math.sqrt(dx * dx + dy * dy); // 10 times faster then previous line
	}

	/**
	 * Returns distance between two 3D points
	 * 
	 * @param point1
	 *          first point
	 * @param point2
	 *          second point
	 * @return distance between points
	 */
	public static double getDistance(Point3D point1, Point3D point2) {
		if (point1 == null || point2 == null)
			return 0;

		return getDistance(point1.getX(), point1.getY(), point1.getZ(), point2.getX(), point2.getY(), point2.getZ());
	}

	/**
	 * Returns distance between 3D set of coords
	 * 
	 * @param x1
	 *          first x coord
	 * @param y1
	 *          first y coord
	 * @param z1
	 *          first z coord
	 * @param x2
	 *          second x coord
	 * @param y2
	 *          second y coord
	 * @param z2
	 *          second z coord
	 * @return distance between coords
	 */
	public static double getDistance(float x1, float y1, float z1, float x2, float y2, float z2) {
		float dx = x1 - x2;
		float dy = y1 - y2;
		float dz = z1 - z2;

		// We should avoid Math.pow or Math.hypot due to perfomance reasons
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * @param object
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static double getDistance(VisibleObject object, float x, float y, float z) {
		return getDistance(object.getX(), object.getY(), object.getZ(), x, y, z);
	}

	/**
	 * @param requester
	 * @param responder
	 * @return
	 */
	public static double getDistance(VisibleObject object, VisibleObject object2) {
		return getDistance(object.getX(), object.getY(), object.getZ(), object2.getX(), object2.getY(), object2.getZ());
	}

	/**
	 * Returns closest point on segment to point
	 * 
	 * @param ss
	 *          segment start point
	 * @param se
	 *          segment end point
	 * @param p
	 *          point to found closest point on segment
	 * @return closest point on segment to p
	 */
	public static Point2D getClosestPointOnSegment(Point ss, Point se, Point p) {
		return getClosestPointOnSegment(ss.x, ss.y, se.x, se.y, p.x, p.y);
	}

	/**
	 * Returns closest point on segment to point
	 * 
	 * @param sx1
	 *          segment x coord 1
	 * @param sy1
	 *          segment y coord 1
	 * @param sx2
	 *          segment x coord 2
	 * @param sy2
	 *          segment y coord 2
	 * @param px
	 *          point x coord
	 * @param py
	 *          point y coord
	 * @return closets point on segment to point
	 */
	public static Point2D getClosestPointOnSegment(float sx1, float sy1, float sx2, float sy2, float px, float py) {
		double xDelta = sx2 - sx1;
		double yDelta = sy2 - sy1;

		if ((xDelta == 0) && (yDelta == 0)) {
			throw new IllegalArgumentException("Segment start equals segment end");
		}

		double u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

		final Point2D closestPoint;
		if (u < 0) {
			closestPoint = new Point2D(sx1, sy1);
		} else if (u > 1) {
			closestPoint = new Point2D(sx2, sy2);
		} else {
			closestPoint = new Point2D((float) (sx1 + u * xDelta), (float) (sy1 + u * yDelta));
		}

		return closestPoint;
	}

	/**
	 * Returns distance to segment
	 * 
	 * @param ss
	 *          segment start point
	 * @param se
	 *          segment end point
	 * @param p
	 *          point to found closest point on segment
	 * @return distance to segment
	 */
	public static double getDistanceToSegment(Point ss, Point se, Point p) {
		return getDistanceToSegment(ss.x, ss.y, se.x, se.y, p.x, p.y);
	}

	/**
	 * Returns distance to segment
	 * 
	 * @param sx1
	 *          segment x coord 1
	 * @param sy1
	 *          segment y coord 1
	 * @param sx2
	 *          segment x coord 2
	 * @param sy2
	 *          segment y coord 2
	 * @param px
	 *          point x coord
	 * @param py
	 *          point y coord
	 * @return distance to segment
	 */
	public static double getDistanceToSegment(int sx1, int sy1, int sx2, int sy2, int px, int py) {
		Point2D closestPoint = getClosestPointOnSegment(sx1, sy1, sx2, sy2, px, py);
		return getDistance(closestPoint.getX(), closestPoint.getY(), px, py);
	}

	/**
	 * Checks whether two given instances of AionObject are within given range.
	 * 
	 * @param object1
	 * @param object2
	 * @param range
	 * @return true if objects are in range, false otherwise
	 */
	public static boolean isInRange(VisibleObject object1, VisibleObject object2, float range) {
		if (object1.getWorldId() != object2.getWorldId() || object1.getInstanceId() != object2.getInstanceId())
			return false;

		float dx = (object2.getX() - object1.getX());
		float dy = (object2.getY() - object1.getY());
		return dx * dx + dy * dy < range * range;
	}

	/**
	 * Checks whether two given instances of AionObject are within given range. Includes Z-Axis check.
	 * 
	 * @param object1
	 * @param object2
	 * @param range
	 * @return true if objects are in range, false otherwise
	 */
	public static boolean isIn3dRange(VisibleObject object1, VisibleObject object2, float range) {
		if (object1.getWorldId() != object2.getWorldId() || object1.getInstanceId() != object2.getInstanceId())
			return false;

		float dx = (object2.getX() - object1.getX());
		float dy = (object2.getY() - object1.getY());
		float dz = (object2.getZ() - object1.getZ());
		return dx * dx + dy * dy + dz * dz < range * range;
	}

	public static boolean isIn3dRangeLimited(VisibleObject object1, VisibleObject object2, float minRange, float maxRange) {
		if (object1.getWorldId() != object2.getWorldId() || object1.getInstanceId() != object2.getInstanceId())
			return false;

		float dx = (object2.getX() - object1.getX());
		float dy = (object2.getY() - object1.getY());
		float dz = (object2.getZ() - object1.getZ());
		return dx * dx + dy * dy + dz * dz > minRange * minRange && dx * dx + dy * dy + dz * dz < maxRange * maxRange;
	}

	/**
	 * @param obj1X
	 * @param obj1Y
	 * @param obj1Z
	 * @param obj2X
	 * @param obj2Y
	 * @param obj2Z
	 * @param range
	 * @return boolean
	 */
	public static boolean isIn3dRange(final float obj1X, final float obj1Y, final float obj1Z, final float obj2X, final float obj2Y, final float obj2Z,
		float range) {
		float dx = (obj2X - obj1X);
		float dy = (obj2Y - obj1Y);
		float dz = (obj2Z - obj1Z);
		return dx * dx + dy * dy + dz * dz < range * range;
	}

	/**
	 * Check Coordinate with formula: " sqrt((x-x0)^2 + (y-y0)^2 + (z-z0)^2) < radius "
	 * 
	 * @param obj
	 * @param centerX
	 * @param centerY
	 * @param centerZ
	 * @param radius
	 * @return true if the object is in the sphere
	 */
	public static boolean isInSphere(final VisibleObject obj, final float centerX, final float centerY, final float centerZ, final float radius) {
		float dx = (obj.getX() - centerX);
		float dy = (obj.getY() - centerY);
		float dz = (obj.getZ() - centerZ);
		return dx * dx + dy * dy + dz * dz < radius * radius;
	}

	/**
	 * Get an angle between the line defined by two points and the horizontal axis
	 */
	public final static float calculateAngleFrom(float obj1X, float obj1Y, float obj2X, float obj2Y) {
		float angleTarget = (float) Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0)
			angleTarget = 360 + angleTarget;
		return angleTarget;
	}

	/**
	 * Get an angle between the line defined by two objects and the horizontal axis
	 */
	public static float calculateAngleFrom(VisibleObject obj1, VisibleObject obj2) {
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}

	/**
	 * @param clientHeading
	 * @return float
	 */
	public final static float convertHeadingToDegree(byte clientHeading) {
		float degree = clientHeading * 3;
		return degree;
	}

	/**
	 * @param clientHeading
	 * @return float
	 */
	public final static byte convertDegreeToHeading(float angle) {
		return (byte) (angle / 3);
	}

	/**
	 * @param obj
	 * @param x
	 * @param y
	 * @param z
	 * @param offset
	 * @return
	 */
	public final static boolean isNearCoordinates(VisibleObject obj, float x, float y, float z, int offset) {
		return getDistance(obj.getX(), obj.getY(), obj.getZ(), x, y, z) < offset + NpcMoveController.MOVE_CHECK_OFFSET;
	}

	/**
	 * @param obj
	 * @param x
	 * @param y
	 * @param z
	 * @param offset
	 * @return
	 */
	public final static boolean isNearCoordinates(VisibleObject obj, VisibleObject obj2, int offset) {
		return getDistance(obj.getX(), obj.getY(), obj.getZ(), obj2.getX(), obj2.getY(), obj2.getZ()) < offset + NpcMoveController.MOVE_CHECK_OFFSET;
	}

	public final static boolean isInAttackRange(Creature object1, Creature object2, float range) {
		if (object1 == null || object2 == null)
			return false;
		if (object1.getWorldId() != object2.getWorldId() || object1.getInstanceId() != object2.getInstanceId()) {
			return false;
		}
		float offset = object1.getObjectTemplate().getBoundRadius().getCollision() + object2.getObjectTemplate().getBoundRadius().getCollision();
		if (object1.getMoveController().isInMove())
			offset = +3f;
		if (object2.getMoveController().isInMove())
			offset = +3f;
		return ((getDistance(object1, object2) - offset) <= range);
	}

	public final static boolean isInsideAttackCylinder(VisibleObject obj1, VisibleObject obj2, int length, int radius, AreaDirections directions) {
		double radian = Math.toRadians(convertHeadingToDegree(obj1.getHeading()));
		int direction = directions == AreaDirections.FRONT ? 0 : 1;
		float dx = (float) (Math.cos(Math.PI * direction + radian) * length);
		float dy = (float) (Math.sin(Math.PI * direction + radian) * length);

		float tdx = obj2.getX() - obj1.getX();
		float tdy = obj2.getY() - obj1.getY();
		float tdz = obj2.getZ() - obj1.getZ();
		float lengthSqr = length * length;

		float dot = tdx * dx + tdy * dy;
		if (dot < 0.0f || dot > lengthSqr)
			return false;

		// distance squared to the cylinder axis
		return (tdx * tdx + tdy * tdy + tdz * tdz) - dot * dot / lengthSqr <= radius;
	}

	/**
	 * Returns the correctly rounded square root of a positive BigDecimal. The algorithm for taking the square root of a BigDecimal is most critical for
	 * the speed of your application. This method performs the fast Square Root by Coupled Newton Iteration algorithm by Timm Ahrendt, from the book
	 * "Pi, unleashed" by Jörg Arndt in a neat loop.
	 * 
	 * @param squarD
	 *          number to get the root from (called "d" in the book)
	 * @param rootMC
	 *          precision and rounding mode (for the last root "x")
	 * @return the root of the argument number
	 * @throws ArithmeticException
	 *           if the argument number is negative
	 * @throws IllegalArgumentException
	 *           if rootMC has precision 0
	 */

	static final BigDecimal TWO = new BigDecimal(2);
	static final double SQRT_10 = 3.162277660168379332;

	public static BigDecimal bigSqrt(BigDecimal squarD, MathContext rootMC) {
		// General number and precision checking
		int sign = squarD.signum();
		if (sign == -1)
			throw new ArithmeticException("\nSquare root of a negative number: " + squarD);
		else if (sign == 0)
			return squarD.round(rootMC);

		int prec = rootMC.getPrecision(); // the requested precision
		if (prec == 0)
			throw new IllegalArgumentException("\nMost roots won't have infinite precision = 0");

		// Initial precision is that of double numbers 2^63/2 ~ 4E18
		int BITS = 62; // 63-1 an even number of number bits
		int nInit = 16; // precision seems 16 to 18 digits
		MathContext nMC = new MathContext(18, RoundingMode.HALF_DOWN);

		// Iteration variables, for the square root x and the reciprocal v
		BigDecimal x = null, e = null; // initial x: x0 ~ sqrt()
		BigDecimal v = null, g = null; // initial v: v0 = 1/(2*x)

		// Estimate the square root with the foremost 62 bits of squarD
		BigInteger bi = squarD.unscaledValue(); // bi and scale are a tandem
		int biLen = bi.bitLength();
		int shift = Math.max(0, biLen - BITS + (biLen % 2 == 0 ? 0 : 1)); // even shift..
		bi = bi.shiftRight(shift); // ..floors to 62 or 63 bit BigInteger

		double root = Math.sqrt(bi.doubleValue());
		BigDecimal halfBack = new BigDecimal(BigInteger.ONE.shiftLeft(shift / 2));

		int scale = squarD.scale();
		if (scale % 2 == 1) // add half scales of the root to odds..
			root *= SQRT_10; // 5 -> 2, -5 -> -3 need half a scale more..
		scale = (int) Math.floor(scale / 2.); // ..where 100 -> 10 shifts the scale

		// Initial x - use double root - multiply by halfBack to unshift - set new scale
		x = new BigDecimal(root, nMC);
		x = x.multiply(halfBack, nMC); // x0 ~ sqrt()
		if (scale != 0)
			x = x.movePointLeft(scale);

		if (prec < nInit) // for prec 15 root x0 must surely be OK
			return x.round(rootMC); // return small prec roots without iterations

		// Initial v - the reciprocal
		v = BigDecimal.ONE.divide(TWO.multiply(x), nMC); // v0 = 1/(2*x)

		// Collect iteration precisions beforehand
		List<Integer> nPrecs = new FastTable<Integer>();

		assert nInit > 3 : "Never ending loop!"; // assume nInit = 16 <= prec

		// Let m be the exact digits precision in an earlier! loop
		for (int m = prec + 1; m > nInit; m = m / 2 + (m > 100 ? 1 : 2))
			nPrecs.add(m);

		// The loop of "Square Root by Coupled Newton Iteration" for simpletons
		for (int i = nPrecs.size() - 1; i > -1; i--) {
			// Increase precision - next iteration supplies n exact digits
			nMC = new MathContext(nPrecs.get(i), (i % 2 == 1) ? RoundingMode.HALF_UP : RoundingMode.HALF_DOWN);

			// Next x // e = d - x^2
			e = squarD.subtract(x.multiply(x, nMC), nMC);
			if (i != 0)
				x = x.add(e.multiply(v, nMC)); // x += e*v ~ sqrt()
			else {
				x = x.add(e.multiply(v, rootMC), rootMC); // root x is ready!
				break;
			}

			// Next v // g = 1 - 2*x*v
			g = BigDecimal.ONE.subtract(TWO.multiply(x).multiply(v, nMC));

			v = v.add(g.multiply(v, nMC)); // v += g*v ~ 1/2/sqrt()
		}

		return x; // return sqrt(squarD) with precision of rootMC
	}
}
