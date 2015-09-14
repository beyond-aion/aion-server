package com.aionemu.gameserver.geoEngine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

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
import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorTemplate;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorWorld;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.ZoneService;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Matrix3f;
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
	private static boolean DEBUG = false;

	public static Map<String, Spatial> loadMeshes(String fileName) {
		Map<String, Spatial> geoms = new FastMap<>();
		File geoFile = new File(GEO_DIR + fileName);
		FileChannel roChannel = null;
		MappedByteBuffer geo = null;
		try (RandomAccessFile file = new RandomAccessFile(geoFile, "r")) {
			roChannel = file.getChannel();
			int size = (int) roChannel.size();
			geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
			geo.order(ByteOrder.LITTLE_ENDIAN);
			while (geo.hasRemaining()) {
				short namelength = geo.getShort();
				byte[] nameByte = new byte[namelength];
				geo.get(nameByte);
				String name = new String(nameByte).intern();
				NodeEx node = new NodeEx(DEBUG ? name : null);
				byte intentions = 0;
				byte singleChildMaterialId = -1;
				int modelCount = geo.getShort();
				for (int c = 0; c < modelCount; c++) {
					MeshEx m = new MeshEx();

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

					GeometryEx geom = null;
					m.setCollisionFlags(geo.getShort());
					if ((m.getIntentions() & CollisionIntention.MOVEABLE.getId()) != 0) {
						// TODO: skip moveable collisions (ships, shugo boxes), not handled yet
						continue;
					}
					intentions |= m.getIntentions();
					m.setBuffer(VertexBuffer.Type.Position, 3, vertices);
					m.setBuffer(VertexBuffer.Type.Index, 3, indexes);
					m.createCollisionData();

					if ((intentions & CollisionIntention.DOOR.getId()) != 0 && (intentions & CollisionIntention.PHYSICAL.getId()) != 0) {
						if (!GeoDataConfig.GEO_DOORS_ENABLE)
							continue;
						// Ignore mesh for now, should set sizes to 0 in geodata parser
						geom = new DoorGeometry(name);
					} else {
						MaterialTemplate mtl = DataManager.MATERIAL_DATA.getTemplate(m.getMaterialId());
						geom = new GeometryEx(null, m);
						if (mtl != null || m.getMaterialId() == 11) {
							node.setName(name);
						}
						if (modelCount == 1) {
							geom.setName(name);
							singleChildMaterialId = geom.getMaterialId();
						} else
							geom.setName(("child" + c + "_" + name).intern());
						node.attachChild(geom);
					}
					geoms.put(geom.getName(), geom);
				}
				node.setCollisionFlags((short) (intentions << 8 | singleChildMaterialId & 0xFF));
				if (!node.getChildren().isEmpty()) {
					geoms.put(name, node);
				}
			}
			destroyDirectByteBuffer(geo);
		} catch (IOException e) {
			log.error("Error loading meshes", e);
		}
		return geoms;
	}

	public static boolean loadWorld(int worldId, Map<String, Spatial> models, GeoMap map) throws IOException {
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
				short nameLength = geo.getShort();
				byte[] nameByte = new byte[nameLength];
				geo.get(nameByte);
				String name = new String(nameByte);
				Vector3f loc = new Vector3f(geo.getFloat(), geo.getFloat(), geo.getFloat());
				float[] matrix = new float[9];
				for (int i = 0; i < 9; i++)
					matrix[i] = geo.getFloat();
				float scale = geo.getFloat();
				Matrix3f matrix3f = new Matrix3f();
				matrix3f.set(matrix);
				Spatial node = models.get(name.toLowerCase().intern());
				if (node != null) {
					Spatial nodeClone = node.deepClone();
					if (nodeClone instanceof DoorGeometry) {
						if (createDoors((DoorGeometry) nodeClone, worldId, matrix3f, loc, scale))
							map.attachChild(nodeClone);
					} else {
						attachChild(map, nodeClone, matrix3f, loc, scale);
						List<Spatial> children = ((Node) nodeClone).descendantMatches("child\\d+_" + name.replace("\\", "\\\\"));
						if (children.size() == 0) {
							createZone(nodeClone, worldId, 0);
						} else {
							for (int c = 0; c < children.size(); c++) {
								Spatial childClone = children.get(c).deepClone();
								attachChild(map, childClone, matrix3f, loc, scale);
								createZone(childClone, worldId, c + 1);
							}
						}
					}
				} else {
					log.warn("Missing mesh for world " + worldId + ": " + name.toLowerCase().intern());
				}
			}
			destroyDirectByteBuffer(geo);
			map.updateGeometricState();
		}
		return true;
	}

	private static void attachChild(GeoMap map, Spatial node, Matrix3f matrix, Vector3f location, float scale) {
		node.setLocalRotation(matrix);
		node.setLocalTranslation(location);
		node.setLocalScale(scale);
		map.attachChild(node);
	}

	private static void createZone(Spatial node, int worldId, int childNumber) {
		if (GeoDataConfig.GEO_MATERIALS_ENABLE)
			if (node instanceof SpatialEx) {
				if ((CollisionIntention.MATERIAL.getId() & ((SpatialEx) node).getIntentions()) != 0) {
					BoundingVolume bv = node.getWorldBound();
					int regionId = getVectorHash(bv.getCenter().x, bv.getCenter().y, bv.getCenter().z);
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
						ZoneService.getInstance().createMaterialZoneTemplate(node, worldId, ((SpatialEx) node).getMaterialId(), true);
					} else {
						node.setName(zoneName);
						ZoneService.getInstance().createMaterialZoneTemplate(node, regionId, worldId, ((SpatialEx) node).getMaterialId());
					}
				}
			} else {
				throw new AssertionError(node.getClass().getSimpleName() + " " + node.getName() + " is not a member of SpatialEx");
			}
	}

	/**
	 * This method creates boxes for doors. Any geo meshes are ignored, however, the bounds are rechecked for boxes during the creation. Boxes are more
	 * efficient way to handle collisions. So, it replaces geo mesh with the artfitial Box geometry, taken from the static_doors.xml. Basically, you can
	 * create missing doors here but that is not implemented. matrix is never used, as well as scale. Those two arguments are needed if location MUST be
	 * repositioned according to geo mesh. Geo may have both meshes from mission xml and from other files, so you never know. But now we handle just
	 * mission xml. Other meshes are ignored.
	 */
	private static boolean createDoors(DoorGeometry geom, int worldId, Matrix3f matrix, Vector3f location, float scale) {
		StaticDoorWorld worldDoors = DataManager.STATICDOOR_DATA.getStaticDoorWorlds(worldId);

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
			MeshEx mesh = new MeshEx();
			mesh.setBound(new Box(boundingBox.getMin(new Vector3f()), boundingBox.getMax(new Vector3f())).getBound());
			geom.setMesh(mesh);
			// Assign flags if they are missing in geo (geo bugs)
			geom.setCollisionFlags((short) ((CollisionIntention.DOOR.getId() | CollisionIntention.PHYSICAL.getId()) << 8));
			break;
		}
		if (geom.getMesh() == null) {
			log.warn("No template for geo door " + geom.getName() + " (map=" + worldId + "; pos=" + location.toString() + ")");
			return false;
		}

		// Here if geo has a real mesh, it expands as big as it should be, so replacement falls back to geo mesh
		BoundingVolume bv = geom.getWorldBound();
		int regionId = getVectorHash(bv.getCenter().x, bv.getCenter().y, bv.getCenter().z);
		int index = geom.getName().lastIndexOf('\\');
		// Create a unique name for doors
		String doorName = worldId + "_" + "DOOR" + "_" + regionId + "_" + geom.getName().substring(index + 1).toUpperCase();
		geom.setName(doorName);
		return true;
	}

	/**
	 * Hash formula from paper <a href="http://www.beosil.com/download/CollisionDetectionHashing_VMV03.pdf"> Optimized Spatial Hashing for Collision
	 * Detection of Deformable Objects</a><br>
	 * Hash table size 900000, the higher value, more precision
	 */
	private static int getVectorHash(float x, float y, float z) {
		long xIntBits = Float.floatToIntBits(x);
		long yIntBits = Float.floatToIntBits(y);
		long zIntBits = Float.floatToIntBits(z);
		return (int) ((xIntBits * 73856093 ^ yIntBits * 19349663 ^ zIntBits * 83492791) % 900000);
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
