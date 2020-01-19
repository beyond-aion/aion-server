package com.aionemu.gameserver.geoEngine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.math.Matrix3f;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.*;
import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.ZoneService;

/**
 * @author Mr. Poke, Neon
 * @update Yeats 13.01.20
 */
public class GeoWorldLoader {

	private static final Logger log = LoggerFactory.getLogger(GeoWorldLoader.class);

	private static String GEO_DIR = "data/geo/";

	private static boolean DEBUG = false;

	public static Map<String, Node> loadMeshes(String fileName) throws IOException {
		Map<String, Node> geoms = new HashMap<>();
		File geoFile = new File(fileName);
		try (RandomAccessFile file = new RandomAccessFile(geoFile, "r"); FileChannel roChannel = file.getChannel()) {
			int size = (int) roChannel.size();
			MappedByteBuffer geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size).load();
			geo.order(ByteOrder.LITTLE_ENDIAN);
			while (geo.hasRemaining()) {
				short namelenght = geo.getShort();
				byte[] nameByte = new byte[namelenght];
				geo.get(nameByte);
				String name = new String(nameByte).replace('\\', '/').toLowerCase().intern();
				Node node = new Node(DEBUG ? name : null);
				byte intentions = 0;
				int singleChildMaterialId = 0;
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
					Geometry geom;
					m.setMaterialId(geo.get());
					m.setCollisionIntentions(geo.get());
					intentions |= m.getCollisionIntentions();
					m.setBuffer(VertexBuffer.Type.Position, 3, vertices);
					m.setBuffer(VertexBuffer.Type.Index, 3, indexes);
					m.createCollisionData();

					MaterialTemplate mtl = DataManager.MATERIAL_DATA.getTemplate(m.getMaterialId());
					geom = new Geometry(name, m);
					if (mtl != null || m.getMaterialId() == 11) {
						node.setName(name);
					}
					if (modelCount == 1)
						singleChildMaterialId = geom.getMaterialId();

					node.attachChild(geom);
				}
				node.setCollisionIntentions(intentions);
				node.setMaterialId((byte) singleChildMaterialId);
				geoms.put(name, node);
			}
			destroyDirectByteBuffer(geo);
		}
		return geoms;

	}

	public static boolean loadWorld(int worldId, Map<String, Node> models, GeoMap map, Set<String> missingMeshes) {
		File geoFile = new File(GEO_DIR + worldId + ".geo");
		try (RandomAccessFile file = new RandomAccessFile(geoFile, "r"); FileChannel roChannel = file.getChannel()) {
			MappedByteBuffer geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) roChannel.size()).load();
			geo.order(ByteOrder.LITTLE_ENDIAN);
			if (geo.get() == 0)
				map.setTerrainData(new short[] { geo.getShort() });
			else {
				int size = geo.getInt();
				short[] terrainData = new short[size];
				byte[] terrainMaterials = new byte[size];
				short z = 0;
				byte mat;
				boolean isAllSameZ = true;
				boolean containsMat = false;
				for (int i = 0; i < size; i++) {
					if (z != (z = geo.getShort()) && i > 0)
						isAllSameZ = false;
					terrainData[i] = z;
					mat = geo.get();
					terrainMaterials[i] = mat;
					if ((mat & 0xFF) > 0) {
						containsMat = true;
					}
				}
				if (isAllSameZ)
					map.setTerrainData(new short[] { z }); // save memory by setting only one z coordinate
				else
					map.setTerrainData(terrainData);
				if (containsMat) {
					map.setTerrainMaterials(terrainMaterials);
				}
			}

			while (geo.hasRemaining()) {
				int nameLength = geo.getShort();
				byte[] nameByte = new byte[nameLength];
				geo.get(nameByte);
				String name = new String(nameByte).replace('\\', '/').toLowerCase();
				Vector3f loc = new Vector3f(geo.getFloat(), geo.getFloat(), geo.getFloat());
				float[] matrix = new float[9];
				for (int i = 0; i < 9; i++)
					matrix[i] = geo.getFloat();
				Vector3f scale = new Vector3f(1f, 1f, 1f);
				scale.setX(geo.getFloat());
				scale.setY(geo.getFloat());
				scale.setZ(geo.getFloat());
				byte type = geo.get();
				short id = geo.getShort();
				byte level = geo.get();
				Matrix3f matrix3f = new Matrix3f();
				matrix3f.set(matrix);
				Node node = models.get(name);
				if (node != null) {
					try {
						if (type > 0) {
							DespawnableNode despawnableNode = new DespawnableNode();
							despawnableNode.copyFrom(node);
							despawnableNode.type = DespawnableNode.DespawnableType.getTypeWithId(type);
							despawnableNode.id = id;
							despawnableNode.level = level;
							node = despawnableNode;
						}
						Node nodeClone = (Node) attachChild(map, node, matrix3f, loc, scale);
						List<Spatial> children = nodeClone.getChildren();
						for (int c = 0; c < children.size(); c++) {
							createZone(children.get(c), worldId, children.size() == 1 ? 0 : c + 1);
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

	private static Spatial attachChild(GeoMap map, Spatial node, Matrix3f matrix, Vector3f location, Vector3f scale) throws CloneNotSupportedException {
		Spatial nodeClone = node.clone();
		nodeClone.setTransform(matrix, location, scale);
		nodeClone.updateModelBound();
		map.attachChild(nodeClone);
		return nodeClone;
	}

	private static void createZone(Spatial geometry, int worldId, int childNumber) {
		if (GeoDataConfig.GEO_MATERIALS_ENABLE && (geometry.getCollisionIntentions() & CollisionIntention.MATERIAL.getId()) != 0) {
			int regionId = getVectorHash(geometry.getWorldBound().getCenter());
			int index = geometry.getName().lastIndexOf('/');
			int dotIndex = geometry.getName().lastIndexOf('.');
			String name = geometry.getName().substring(index + 1, dotIndex).toUpperCase();
			if (childNumber > 0)
				name += "_CHILD" + childNumber;
			geometry.setName(name + "_" + regionId);
			ZoneName zoneName = ZoneName.createOrGet(geometry.getName() + "_" + worldId);
			ZoneService.getInstance().createMaterialZoneTemplate(geometry, worldId, zoneName);
		}
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
	}
}
