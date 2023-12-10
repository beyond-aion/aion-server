package com.aionemu.gameserver.geoEngine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.math.Matrix3f;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.*;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.ZoneService;

/**
 * @author Mr. Poke, Neon, Yeats
 */
public class GeoWorldLoader {

	private static final Logger log = LoggerFactory.getLogger(GeoWorldLoader.class);
	private static final String GEO_DIR = "data/geo/";

	public static void load(Collection<GeoMap> maps) {
		load(maps, loadMeshes());
		// preload mesh collision data for responsive initial collision checks and predictable memory usage
		ThreadPoolManager.getInstance()
			.execute(() -> maps.parallelStream().flatMap(m -> m.getGeometries().map(Geometry::getMesh)).distinct().forEach(Mesh::createCollisionData));
	}

	private static void load(Collection<GeoMap> maps, Map<String, Node> models) {
		log.info("Loading geo maps...");
		Set<String> missingMeshes = ConcurrentHashMap.newKeySet();
		maps.parallelStream().forEach(map -> loadWorld(map, models, missingMeshes));
		if (!missingMeshes.isEmpty())
			log.warn(missingMeshes.size() + " meshes are missing:\n" + missingMeshes.stream().sorted().collect(Collectors.joining("\n")));
		log.info("Loaded " + maps.size() + " geo maps.");
	}

	private static Map<String, Node> loadMeshes() {
		log.info("Loading meshes...");
		File[] meshFiles = new File(GEO_DIR).listFiles((file, name) -> name.toLowerCase().endsWith(".mesh"));
		if (meshFiles == null || meshFiles.length == 0) {
			log.warn("No *.mesh files present in ./" + GEO_DIR);
			return Collections.emptyMap();
		}
		Map<String, Node> meshes = new HashMap<>();
		for (File meshFile : meshFiles)
			meshes.putAll(loadMeshes(meshFile));
		return meshes;
	}

	private static Map<String, Node> loadMeshes(File meshFile) {
		Map<String, Node> geoms = new HashMap<>();
		try (RandomAccessFile file = new RandomAccessFile(meshFile, "r"); FileChannel roChannel = file.getChannel()) {
			MappedByteBuffer geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, roChannel.size()).load();
			while (geo.hasRemaining()) {
				short nameLength = geo.getShort();
				byte[] nameByte = new byte[nameLength];
				geo.get(nameByte);
				String name = new String(nameByte);
				Node node = new Node(null);
				byte intentions = 0;
				int singleChildMaterialId = 0;
				int modelCount = geo.get() & 0xFF;
				for (int c = 0; c < modelCount; c++) {
					Mesh m = new Mesh();

					int vectorCount = geo.getShort() & 0xFFFF;
					int vectorByteCount = vectorCount * 3 * 4; // 3 floats per vector (x, y, z), 4 bytes each
					m.setBuffer(VertexBuffer.Type.Position, 3, geo.slice(geo.position(), vectorByteCount).asFloatBuffer());
					geo.position(geo.position() + vectorByteCount);

					int triangleCount = geo.getShort() & 0xFFFF;
					int triangleIndicesByteCount = triangleCount * 3 * 2; // 3 vector indices per triangle, 2 bytes each
					m.setBuffer(VertexBuffer.Type.Index, 3, geo.slice(geo.position(), triangleIndicesByteCount).asShortBuffer());
					geo.position(geo.position() + triangleIndicesByteCount);

					m.setMaterialId(geo.get());
					m.setCollisionIntentions(geo.get());
					intentions |= m.getCollisionIntentions();
					if (node.getName() == null && (m.getMaterialId() == 11 || DataManager.MATERIAL_DATA.getTemplate(m.getMaterialId()) != null))
						node.setName(name);
					if (modelCount == 1)
						singleChildMaterialId = m.getMaterialId();
					node.attachChild(new Geometry(name, m));
				}
				node.setCollisionIntentions(intentions);
				node.setMaterialId((byte) singleChildMaterialId);
				if (!name.contains("|")) {
					geoms.put(name, node);
				} else {
					for (String n : name.split("\\|")) {
						Node clone = node.clone();
						if (clone.getName() != null)
							clone.setName(n);
						clone.getChild(name).setName(n);
						geoms.put(n, clone);
					}
				}
			}
		} catch (IOException | CloneNotSupportedException e) {
			throw new GameServerError("Could not load " + meshFile, e);
		}
		return geoms;
	}

	private static void loadWorld(GeoMap map, Map<String, Node> models, Set<String> missingMeshes) {
		File geoFile = new File(GEO_DIR + map.getMapId() + ".geo");
		if (!geoFile.exists()) {
			if (DataManager.WORLD_MAPS_DATA.getTemplate(map.getMapId()).getWorldSize() != 0) // don't warn about inaccessible (test) maps
				log.warn(geoFile + " is missing");
			return;
		}
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
				String name = new String(nameByte);
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
						createZone(children.get(c), map.getMapId(), children.size() == 1 ? 0 : c + 1);
					}
				} else {
					missingMeshes.add(name);
				}
			}
		} catch (Exception e) {
			throw new GameServerError("Could not load " + geoFile, e);
		}
		map.updateModelBound();
	}

	private static Spatial attachChild(GeoMap map, Spatial node, Matrix3f matrix, Vector3f location, Vector3f scale) throws CloneNotSupportedException {
		Spatial nodeClone = node.clone();
		nodeClone.setTransform(matrix, location, scale);
		nodeClone.updateModelBound();
		map.attachChild(nodeClone);
		return nodeClone;
	}

	private static void createZone(Spatial geometry, int worldId, int childNumber) {
		if ((geometry.getCollisionIntentions() & CollisionIntention.MATERIAL.getId()) != 0) {
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
