package com.aionemu.gameserver.geoEngine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.math.Matrix3f;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.Box;
import com.aionemu.gameserver.geoEngine.scene.Geometry;
import com.aionemu.gameserver.geoEngine.scene.Mesh;
import com.aionemu.gameserver.geoEngine.scene.Node;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.geoEngine.scene.VertexBuffer;
import com.aionemu.gameserver.geoEngine.scene.mesh.DoorGeometry;
import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorTemplate;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorWorld;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.ZoneService;

/**
 * @author Mr. Poke
 * @modified Neon
 */
public class GeoWorldLoader {

	private static final Logger log = LoggerFactory.getLogger(GeoWorldLoader.class);

	private static String GEO_DIR = "data/geo/";

	private static boolean DEBUG = false;

	public static Map<String, Node> loadMeshes(String fileName) throws IOException {
		Map<String, Node> geoms = new HashMap<>();
		File geoFile = new File(fileName);
		FileChannel roChannel = null;
		MappedByteBuffer geo = null;
		try (RandomAccessFile file = new RandomAccessFile(geoFile, "r")) {
			roChannel = file.getChannel();
			int size = (int) roChannel.size();
			geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
			geo.order(ByteOrder.LITTLE_ENDIAN);
			while (geo.hasRemaining()) {
				short namelenght = geo.getShort();
				byte[] nameByte = new byte[namelenght];
				geo.get(nameByte);
				String name = new String(nameByte).replace("/", "\\").toLowerCase().intern();
				Node node = new Node(DEBUG ? name : null);
				byte intentions = 0;
				byte singleChildMaterialId = -1;
				int modelCount = geo.getShort() & 0xFFFF;
				for (int c = 0; c < modelCount; c++) {
					Mesh m = new Mesh();

					int vectorCount = (geo.getShort() & 0xFFFF) * 3;
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

					Geometry geom = null;
					m.setCollisionFlags(geo.getShort());
					intentions |= m.getIntentions();
					m.setBuffer(VertexBuffer.Type.Position, 3, vertices);
					m.setBuffer(VertexBuffer.Type.Index, 3, indexes);
					m.createCollisionData();

					if ((m.getIntentions() & CollisionIntention.DOOR.getId()) != 0 && (m.getIntentions() & CollisionIntention.PHYSICAL.getId()) != 0) {
						geom = new DoorGeometry(name);
					} else {
						MaterialTemplate mtl = DataManager.MATERIAL_DATA.getTemplate(m.getMaterialId());
						geom = new Geometry(name, m);
						if (mtl != null || m.getMaterialId() == 11) {
							node.setName(name);
						}
						if (modelCount == 1)
							singleChildMaterialId = geom.getMaterialId();
					}
					node.attachChild(geom);
				}
				node.setCollisionFlags((short) (intentions << 8 | singleChildMaterialId & 0xFF));
				if (node.getChildren().isEmpty())
					throw new StreamCorruptedException("Cannot read mesh file \"" + fileName + "\": missing geometry data for " + name);
				geoms.put(name, node);
			}
			destroyDirectByteBuffer(geo);
		}
		return geoms;

	}

	public static boolean loadWorld(int worldId, Map<String, Node> models, GeoMap map, Set<String> missingMeshes, List<String> missingDoors) {
		File geoFile = new File(GEO_DIR + worldId + ".geo");
		FileChannel roChannel = null;
		MappedByteBuffer geo = null;

		try (RandomAccessFile file = new RandomAccessFile(geoFile, "r")) {
			roChannel = file.getChannel();
			geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) roChannel.size()).load();
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
				int nameLength = geo.getShort();
				byte[] nameByte = new byte[nameLength];
				geo.get(nameByte);
				String name = new String(nameByte).replace("/", "\\").toLowerCase();
				Vector3f loc = new Vector3f(geo.getFloat(), geo.getFloat(), geo.getFloat());
				float[] matrix = new float[9];
				for (int i = 0; i < 9; i++)
					matrix[i] = geo.getFloat();
				float scale = geo.getFloat();
				Matrix3f matrix3f = new Matrix3f();
				matrix3f.set(matrix);
				Node node = models.get(name);
				if (node != null) {
					try {
						if ((node.getIntentions() & CollisionIntention.DOOR.getId()) != 0) {
							if (!GeoDataConfig.GEO_DOORS_ENABLE) // ignore mesh for now (should be handled in collideWith() so it can be toggled during runtime)
								continue;
							for (Spatial door : node.getChildren()) {
								DoorGeometry doorClone = (DoorGeometry) door.clone();
								if (createDoors(doorClone, worldId, matrix3f, loc, scale))
									map.attachChild(doorClone);
								else
									missingDoors.add(doorClone.getName() + " (map=" + worldId + "; pos=" + loc + ")");
							}
						} else {
							if ((node.getIntentions() & CollisionIntention.MOVEABLE.getId()) != 0) // TODO: handle moveable collisions (ships, shugo boxes)
								continue;
							Node nodeClone = (Node) attachChild(map, node, matrix3f, loc, scale);
							List<Spatial> children = node.getChildren();
							if (children.size() == 1) {
								createZone(nodeClone, worldId, 0);
							} else {
								for (int c = 0; c < children.size(); c++) {
									Spatial child = children.get(c);
									Spatial childClone = attachChild(map, child, matrix3f, loc, scale);
									createZone(childClone, worldId, c + 1);
								}
							}
						}
					} catch (Exception e) {
						log.error("", e);
					}
				} else {
					missingMeshes.add(name);
				}
			}
			destroyDirectByteBuffer(geo);
			map.updateModelBound();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	private static Spatial attachChild(GeoMap map, Spatial node, Matrix3f matrix, Vector3f location, float scale) throws CloneNotSupportedException {
		Spatial nodeClone = node.clone();
		nodeClone.setTransform(matrix, location, scale);
		nodeClone.updateModelBound();
		map.attachChild(nodeClone);
		return nodeClone;
	}

	private static void createZone(Spatial node, int worldId, int childNumber) {
		if (node.getName() == null) {
			return;
		}
		if (GeoDataConfig.GEO_MATERIALS_ENABLE && (node.getIntentions() & CollisionIntention.MATERIAL.getId()) != 0) {
			int regionId = getVectorHash(node.getWorldBound().getCenter());
			int index = node.getName().lastIndexOf('\\');
			int dotIndex = node.getName().lastIndexOf('.');
			String zoneName = node.getName().substring(index + 1, dotIndex).toUpperCase();
			if (childNumber > 0)
				zoneName += "_CHILD" + childNumber;
			String existingName = zoneName + "_" + regionId + "_" + worldId;
			if (ZoneName.getId(existingName) != ZoneName.getId(ZoneName.NONE)) {
				// for override
				zoneName += "_" + regionId;
				node.setName(zoneName);
				ZoneService.getInstance().createMaterialZoneTemplate(node, worldId, node.getMaterialId(), true);
			} else {
				node.setName(zoneName);
				ZoneService.getInstance().createMaterialZoneTemplate(node, regionId, worldId, node.getMaterialId());
			}
		}
	}

	/*
	 * This method creates boxes for doors. Any geo meshes are ignored, however, the bounds are rechecked
	 * for boxes during the creation. Boxes are more efficient way to handle collisions. So, it replaces
	 * geo mesh with the artfitial Box geometry, taken from the static_doors.xml. Basically, you can create
	 * missing doors here but that is not implemented. matrix is never used, as well as scale.
	 * Those two arguments are needed if location MUST be repositioned according to geo mesh.
	 * Geo may have both meshes from mission xml and from other files, so you never know. But now we handle
	 * just mission xml. Other meshes are ignored.
	 */
	private static boolean createDoors(Spatial node, int worldId, Matrix3f matrix, Vector3f location, float scale) {
		DoorGeometry geom = (DoorGeometry) node;

		StaticDoorWorld worldDoors = DataManager.STATICDOOR_DATA.getStaticDoorWorlds(worldId);
		if (worldDoors == null)
			return false;

		for (StaticDoorTemplate template : worldDoors.getStaticDoors()) {
			BoundingBox boundingBox = template.getBoundingBox();
			if (boundingBox == null)
				continue; // absent in static doors templates, don't add it (why? but templates must be updated as well)

			// Check if location is inside the template box. Usually, it's a center of box
			if (!boundingBox.contains(location)) {
				Vector3f templatePos = null;
				// Not inside? Crappy templates, check the position then and do the best
				if (template.getX() != null) {
					// enough to check one coordinate for presence
					templatePos = new Vector3f(template.getX(), template.getY(), template.getZ());
					if (location.distance(templatePos) > 1f)
						continue;
				} else
					continue;
			}

			// Replace geo mesh
			Box boxMesh = new Box(boundingBox.getMin(new Vector3f()), boundingBox.getMax(new Vector3f()));
			geom.setMesh(boxMesh);
			// Assign flags if they are missing in geo (geo bugs)
			geom.setCollisionFlags((short) (CollisionIntention.DEFAULT_COLLISIONS.getId() << 8));
			break;
		}

		if (geom.getMesh() == null)
			return false;

		// Here if geo has a real mesh, it expands as big as it should be, so replacement falls back to geo mesh
		node.updateModelBound();
		int regionId = getVectorHash(node.getWorldBound().getCenter());
		int index = node.getName().lastIndexOf('\\');
		// Create a unique name for doors
		String doorName = worldId + "_DOOR_" + regionId + "_" + node.getName().substring(index + 1).toUpperCase();
		node.setName(doorName);
		return true;
	}

	/**
	 * Hash formula from paper <a href="http://www.beosil.com/download/CollisionDetectionHashing_VMV03.pdf">
	 * Optimized Spatial Hashing for Collision Detection of Deformable Objects</a> found
	 * <a href="http://stackoverflow.com/questions/5928725/hashing-2d-3d-and-nd-vectors">here</a>.<br>
	 * Hash table size is 700001. The higher value, the more precision (works most efficiently if it's a prime number).
	 */
	private static int getVectorHash(Vector3f location) {
		long xIntBits = Float.floatToIntBits(location.x);
		long yIntBits = Float.floatToIntBits(location.y);
		long zIntBits = Float.floatToIntBits(location.z);
		return (int) ((xIntBits * 73856093 ^ yIntBits * 19349669 ^ zIntBits * 83492791) % 700001);
	}

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
