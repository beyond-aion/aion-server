package com.aionemu.gameserver.geoEngine;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.models.DoorGeometry;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.GeometryEx;
import com.aionemu.gameserver.geoEngine.scene.MeshEx;
import com.aionemu.gameserver.geoEngine.scene.NodeEx;
import com.aionemu.gameserver.geoEngine.scene.SpatialEx;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorTemplate;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorWorld;
import com.aionemu.gameserver.world.zone.ZoneService;
import com.jme3.bounding.BoundingBox;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;

/**
 * @author Mr. Poke
 * @modified Neon
 */
public class GeoWorldLoader {

	private static final Logger log = LoggerFactory.getLogger(GeoWorldLoader.class);
	private static String GEO_DIR = "data/geo/";

	public static Map<String, SpatialEx> loadMeshes(String fileName) {
		Map<String, SpatialEx> geoms = new FastMap<>();

		try (RandomAccessFile file = new RandomAccessFile(GEO_DIR + fileName, "r")) {
			FileChannel roChannel = file.getChannel();
			MappedByteBuffer geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) roChannel.size()).load();
			geo.order(ByteOrder.LITTLE_ENDIAN);

			while (geo.hasRemaining()) {
				short namelength = geo.getShort();
				byte[] nameByte = new byte[namelength];
				geo.get(nameByte);
				String name = new String(nameByte).intern();
				NodeEx node = new NodeEx(name);
				byte intentions = 0;
				byte singleChildMaterialId = -1;
				int modelCount = geo.getShort();
				for (int c = 0; c < modelCount; c++) {

					int vectorCount = (geo.getShort()) * 3;
					ByteBuffer floatBuffer = MappedByteBuffer.allocateDirect(vectorCount * 4);
					FloatBuffer vertices = floatBuffer.asFloatBuffer();
					for (int x = 0; x < vectorCount; x++) {
						vertices.put(geo.getFloat());
					}

					int triangles = geo.getInt();
					ByteBuffer shortBuffer = MappedByteBuffer.allocateDirect(triangles * 2);
					ShortBuffer indexes = shortBuffer.asShortBuffer();
					for (int x = 0; x < triangles; x++) {
						indexes.put(geo.getShort());
					}

					MeshEx m = new MeshEx();
					m.setCollisionFlags(geo.getShort());

					intentions |= m.getIntentions();
					m.setBuffer(VertexBuffer.Type.Position, 3, vertices);
					m.setBuffer(VertexBuffer.Type.Index, 3, indexes);

					GeometryEx geom;
					if ((intentions & CollisionIntention.DOOR.getId()) != 0 && (intentions & CollisionIntention.PHYSICAL.getId()) != 0) {
						geoms.put(name, new DoorGeometry(name, m)); // note: door mesh gets replaced with a box in loadWorld method
					} else {
						geom = new GeometryEx(name, m);

						if (modelCount == 1) {
							singleChildMaterialId = geom.getMaterialId();
						} else {
							geom.setName(("child" + (c + 1) + "_" + name).intern());
						}

						if (node.getName() == null && (DataManager.MATERIAL_DATA.getTemplate(m.getMaterialId()) != null || m.getMaterialId() == 11))
							node.setName(name);

						node.attachChild(geom);
					}
				}
				if (node.getQuantity() > 0) {
					node.setCollisionFlags((short) (intentions << 8 | singleChildMaterialId & 0xFF));
					geoms.put(name, node);
				}
			}
			destroyDirectByteBuffer(geo);
		} catch (IOException e) {
			log.error("Error loading meshes", e);
		}
		return geoms;
	}

	public static boolean loadWorld(int worldId, Map<String, SpatialEx> models, GeoMap map, Set<String> missingMeshes) {
		try (RandomAccessFile file = new RandomAccessFile(GEO_DIR + worldId + ".geo", "r")) {
			FileChannel roChannel = file.getChannel();
			MappedByteBuffer geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) roChannel.size()).load();
			geo.order(ByteOrder.LITTLE_ENDIAN);

			if (geo.get() == 0)
				map.setTerrainData(new short[] { geo.getShort() });
			else {
				int size = geo.getInt();
				short[] terrainData = new short[size];
				for (int i = 0; i < size; i++)
					terrainData[i] = geo.getShort();
				map.setTerrainData(terrainData);
			}

			while (geo.hasRemaining()) {
				short nameLength = geo.getShort();
				byte[] nameByte = new byte[nameLength];
				geo.get(nameByte);
				String name = new String(nameByte).intern();
				Vector3f location = new Vector3f(geo.getFloat(), geo.getFloat(), geo.getFloat());
				Quaternion rotation = new Quaternion().fromRotationMatrix(geo.getFloat(), geo.getFloat(), geo.getFloat(), geo.getFloat(), geo.getFloat(),
					geo.getFloat(), geo.getFloat(), geo.getFloat(), geo.getFloat());
				float scale = geo.getFloat();
				Transform t = new Transform(location, rotation, new Vector3f(scale, scale, scale));

				Spatial node = (Spatial) models.get(name.toLowerCase().intern());
				if (node != null) {
					if ((CollisionIntention.MOVEABLE.getId() & ((SpatialEx) node).getIntentions()) != 0)
						continue; // TODO skip movable collisions (ships, shugo boxes), not handled yet

					if (node instanceof DoorGeometry) {
						if (!GeoDataConfig.GEO_DOORS_ENABLE)
							continue;
						DoorGeometry clone = (DoorGeometry) node.clone();
						if (createDoors(clone, worldId, location, rotation, scale))
							map.attachChild(clone);
					} else {
						for (Spatial child : ((Node) node).getChildren())
							child.setLocalTransform(t);
						Spatial nodeClone = node.clone();
						map.attachChild(nodeClone);

						if (GeoDataConfig.GEO_MATERIALS_ENABLE) {
							for (Spatial child : ((Node) nodeClone).getChildren())
								createMaterialZone((GeometryEx) child, worldId);
						}
					}
				} else {
					missingMeshes.add(name);
				}
			}
			destroyDirectByteBuffer(geo);
			map.updateModelBound();
			map.updateGeometricState();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	private static void createMaterialZone(GeometryEx geom, int worldId) {
		if ((CollisionIntention.MATERIAL.getId() & geom.getIntentions()) != 0) {
			int regionId = getVectorHash(geom.getWorldBound().getCenter());
			String zoneName = geom.getMeshFileName(false);
			if (geom.getName().startsWith("child"))
				zoneName += "_" + geom.getName().substring(0, geom.getName().indexOf("_")); // appends _CHILD{num}
			zoneName += "_" + regionId;
			geom.setName(zoneName.toUpperCase());
			ZoneService.getInstance().createMaterialZoneTemplate(geom, worldId);
		}
	}

	/**
	 * This method creates boxes for doors. Any geo meshes are ignored, however, the bounds are rechecked for boxes during the creation. Boxes are more
	 * efficient way to handle collisions. So, it replaces geo mesh with the artificial Box geometry, taken from staticdoor_templates.xml. Basically,
	 * you can create missing doors here but that is not implemented. Rotation is never used, as well as scale. Those two arguments are needed if
	 * location MUST be repositioned according to geo mesh. Geo may have both meshes from mission xml and from other files, so you never know. But now
	 * we handle just mission xml. Other meshes are ignored.
	 */
	private static boolean createDoors(DoorGeometry geom, int worldId, Vector3f location, Quaternion rotation, float scale) {
		StaticDoorWorld worldDoors = DataManager.STATICDOOR_DATA.getStaticDoorWorlds(worldId);

		for (StaticDoorTemplate template : worldDoors.getStaticDoors()) {
			BoundingBox boundingBox = template.getBoundingBox();
			if (boundingBox == null)
				continue; // absent in static doors templates, don't add it (why? but templates must be updated as well)

			// Check if location is inside the template box. Usually, it's a center of box
			if (!boundingBox.contains(location)) {
				// Not inside? Crappy templates, check the position then and do the best
				if (template.getX() != null) {
					// enough to check one coordinate for presence
					if (location.distance(new Vector3f(template.getX(), template.getY(), template.getZ())) > 1f)
						continue;
				} else
					continue;
			}

			// Replace door mesh bound
			geom.getMesh().setBound(new Box(boundingBox.getMin(new Vector3f()), boundingBox.getMax(new Vector3f())).getBound());
			// set door location (rotation and scale are ignored since the mesh bound got replaced with an aligned box)
			geom.setLocalTranslation(location);
			geom.updateGeometricState();
			int regionId = getVectorHash(geom.getWorldBound().getCenter());
			// Create a unique name for doors
			String doorName = worldId + "_" + "DOOR" + "_" + regionId + "_" + geom.getMeshFileName().toUpperCase();
			geom.setName(doorName);
			return true;
		}

		log.warn("No template for geo door " + geom.getName() + " (map=" + worldId + "; pos=" + location.toString() + ")");
		return false;
	}

	/**
	 * Hash formula from paper <a href="http://www.beosil.com/download/CollisionDetectionHashing_VMV03.pdf">Optimized Spatial Hashing for Collision
	 * Detection of Deformable Objects</a> found <a href="http://stackoverflow.com/questions/5928725/hashing-2d-3d-and-nd-vectors">here</a>.<br>
	 * Hash table size is 700001. The higher value, more precision (works most efficiently if it's a prime number).
	 */
	private static int getVectorHash(Vector3f location) {
		long xIntBits = Float.floatToIntBits(location.x);
		long yIntBits = Float.floatToIntBits(location.y);
		long zIntBits = Float.floatToIntBits(location.z);
		return (int) ((xIntBits * 73856093 ^ yIntBits * 19349669 ^ zIntBits * 83492791) % 700001);
	}

	/**
	 * DirectByteBuffers are garbage collected by using a phantom reference and a reference queue. Every once a while, the JVM checks the reference
	 * queue and cleans the DirectByteBuffers. However, as this doesn't happen immediately after discarding all references to a DirectByteBuffer, it's
	 * easy to OutOfMemoryError yourself using DirectByteBuffers. This function explicitly calls the Cleaner method of a DirectByteBuffer.
	 * 
	 * @param toBeDestroyed
	 *          The DirectByteBuffer that will be "cleaned". Utilizes reflection.
	 */
	private static void destroyDirectByteBuffer(ByteBuffer toBeDestroyed) {
		if (!toBeDestroyed.isDirect())
			return;

		try {
			Method cleaner = toBeDestroyed.getClass().getMethod("cleaner");
			cleaner.setAccessible(true);
			Method clean = Class.forName("sun.misc.Cleaner").getMethod("clean");
			clean.setAccessible(true);
			clean.invoke(cleaner.invoke(toBeDestroyed));
		} catch (Exception ex) {
		}
		toBeDestroyed = null;
	}
}
