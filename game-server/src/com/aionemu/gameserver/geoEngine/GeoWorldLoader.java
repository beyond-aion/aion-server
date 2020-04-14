package com.aionemu.gameserver.geoEngine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.math.Matrix3f;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.*;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.DummyGeoData;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.ZoneService;

/**
 * @author Mr. Poke, Neon, Yeats
 */
public class GeoWorldLoader {

	private static final String GEO_DIR = "data/geo/";
	private static final boolean DEBUG = false;

	public static Map<String, Node> loadMeshes(String fileName) throws IOException {
		List<CountDownLatch> latches = new ArrayList<>();
		Map<String, Node> geoms = new HashMap<>();
		File geoFile = new File(fileName);
		try (RandomAccessFile file = new RandomAccessFile(geoFile, "r"); FileChannel roChannel = file.getChannel()) {
			MappedByteBuffer geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, roChannel.size()).load();
			while (geo.hasRemaining()) {
				short namelenght = geo.getShort();
				byte[] nameByte = new byte[namelenght];
				geo.get(nameByte);
				String name = new String(nameByte).replace('\\', '/').toLowerCase().intern();
				Node node = new Node(DEBUG ? name : null);
				byte intentions = 0;
				int singleChildMaterialId = 0;
				int modelCount = geo.getShort() & 0xFFFF;
				CountDownLatch latch = new CountDownLatch(modelCount);
				latches.add(latch);
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
					m.setMaterialId(geo.get());
					m.setCollisionIntentions(geo.get());
					intentions |= m.getCollisionIntentions();
					m.setBuffer(VertexBuffer.Type.Position, 3, vertices);
					m.setBuffer(VertexBuffer.Type.Index, 3, indexes);
					if (node.getName() == null && (m.getMaterialId() == 11 || DataManager.MATERIAL_DATA.getTemplate(m.getMaterialId()) != null))
						node.setName(name);
					if (modelCount == 1)
						singleChildMaterialId = m.getMaterialId();
					ThreadPoolManager.getInstance().execute(() -> {
						m.createCollisionData();
						Geometry geom = new Geometry(name, m);
						synchronized (node) {
							node.attachChild(geom);
						}
						latch.countDown();
					});
				}
				node.setCollisionIntentions(intentions);
				node.setMaterialId((byte) singleChildMaterialId);
				geoms.put(name, node);
			}
		}
		latches.forEach(l -> {
			try {
				l.await();
			} catch (InterruptedException ignored) {
			}
		});
		return geoms;
	}

	public static GeoMap loadWorld(WorldMapTemplate template, Map<String, Node> models, Set<String> missingMeshes) {
		File geoFile = new File(GEO_DIR + template.getMapId() + ".geo");
		if (!geoFile.exists())
			return DummyGeoData.DUMMY_MAP;
		GeoMap map = new GeoMap(template.getMapId(), template.getWorldSize());
		try (RandomAccessFile file = new RandomAccessFile(geoFile, "r"); FileChannel roChannel = file.getChannel()) {
			MappedByteBuffer geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, roChannel.size()).load();
			if (geo.get() == 0)
				map.setTerrainData(new short[] { geo.getShort() });
			else {
				int size = geo.getInt();
				short[] terrainData = new short[size];
				byte[] terrainMaterials = new byte[size];
				boolean containsMat = false;
				for (int i = 0; i < size; i++) {
					short z = geo.getShort();
					terrainData[i] = z;
					byte mat = geo.get();
					terrainMaterials[i] = mat;
					if ((mat & 0xFF) > 0) {
						containsMat = true;
					}
				}
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
				Matrix3f matrix3f = new Matrix3f();
				for (int i = 0; i < 3; i++)
					for (int j = 0; j < 3; j++)
						matrix3f.set(i, j, geo.getFloat());
				Vector3f scale = new Vector3f(geo.getFloat(), geo.getFloat(), geo.getFloat());
				byte type = geo.get();
				short id = geo.getShort();
				byte level = geo.get();
				Node node = models.get(name);
				if (node != null) {
					if (type > 0) {
						DespawnableNode despawnableNode = new DespawnableNode();
						despawnableNode.copyFrom(node);
						despawnableNode.type = DespawnableNode.DespawnableType.getById(type);
						despawnableNode.id = id;
						despawnableNode.level = level;
						node = despawnableNode;
					}
					Node nodeClone = (Node) attachChild(map, node, matrix3f, loc, scale);
					List<Spatial> children = nodeClone.getChildren();
					for (int c = 0; c < children.size(); c++) {
						createZone(children.get(c), template.getMapId(), children.size() == 1 ? 0 : c + 1);
					}
				} else {
					missingMeshes.add(name);
				}
			}
			map.updateModelBound();
		} catch (Exception e) {
			throw new GameServerError("Could not load " + geoFile, e);
		}
		return map;
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
}
