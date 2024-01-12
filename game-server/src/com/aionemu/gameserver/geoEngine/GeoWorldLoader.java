package com.aionemu.gameserver.geoEngine;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.math.Matrix3f;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.models.Terrain;
import com.aionemu.gameserver.geoEngine.scene.*;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneName;
import com.aionemu.gameserver.world.zone.ZoneService;

/**
 * @author Mr. Poke, Neon, Yeats
 */
public class GeoWorldLoader {

	private static final Logger log = LoggerFactory.getLogger(GeoWorldLoader.class);
	private static final Path GEO_DIR = Path.of("data/geo/");

	public static void load(Collection<GeoMap> maps) throws InterruptedException {
		Thread terrainThread = Thread.ofVirtual().name("terrain").start(() -> loadTerrains(maps));
		load(maps, loadMeshes());
		terrainThread.join();
		// preload mesh collision data for responsive initial collision checks and predictable memory usage
		ThreadPoolManager.getInstance()
			.execute(() -> maps.parallelStream().flatMap(m -> m.getGeometries().map(Geometry::getMesh)).distinct().forEach(Mesh::createCollisionData));
	}

	/**
	 * Loads heightmap and material data from PNG images and assigns it to all maps matching the file name.<br>
	 * Since terrain data is static, it is safe to share it between maps.
	 */
	private static void loadTerrains(Collection<GeoMap> maps) {
		Map<GeoMap, Terrain> terrainByMap = new ConcurrentHashMap<>();
		try {
			Files.find(GEO_DIR, 1, (p, attr) -> attr.isRegularFile() && p.toString().toLowerCase().endsWith(".png")).parallel().forEach(path -> {
				BufferedImage image = null;
				Set<String> mapIds = Stream.of(path.getFileName().toString().split(",")).collect(Collectors.toSet());
				for (GeoMap map : maps) {
					if (mapIds.isEmpty())
						break;
					if (mapIds.removeIf(mapId -> mapId.startsWith(String.valueOf(map.getMapId())))) {
						if (image == null) {
							try {
								image = ImageIO.read(path.toFile());
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
						Terrain terrain = terrainByMap.computeIfAbsent(map, k -> new Terrain());
						switch (image.getRaster().getDataBuffer()) {
							case DataBufferUShort heightmap -> terrain.setHeightmap(heightmap.getData(), image.getWidth(), image.getHeight());
							case DataBufferByte materials -> terrain.setMaterials(materials.getData(), image.getWidth(), image.getHeight());
							default -> log.warn(path + " is not a supported terrain data format");
						}
					}
				}
				mapIds.forEach(mapId -> log.warn(mapId + " of " + path + " could not be associated with a map"));
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		terrainByMap.forEach((map, terrain) -> {
			if (terrain.hasHeightmap())
				map.setTerrain(terrain);
			else
				log.warn("Missing terrain heightmap for " + map.getMapId());
		});
		long terrainMapCount = maps.stream().filter(GeoMap::hasTerrain).count();
		if (terrainMapCount == 0)
			log.warn("No terrains were loaded");
		else
			log.info("Loaded terrains for " + terrainMapCount + " maps");
		if (maps.stream().noneMatch(GeoMap::hasTerrainMaterials))
			log.warn("No terrain materials were loaded");
	}

	private static void load(Collection<GeoMap> maps, Map<String, Node> models) {
		Set<String> missingMeshes = ConcurrentHashMap.newKeySet();
		maps.parallelStream().forEach(map -> loadWorld(map, models, missingMeshes));
		if (!missingMeshes.isEmpty())
			log.warn(missingMeshes.size() + " meshes are missing:\n" + missingMeshes.stream().sorted().collect(Collectors.joining("\n")));
		long loadedMaps = maps.stream().filter(m -> !m.getChildren().isEmpty()).count();
		if (loadedMaps == 0) {
			log.warn("No geo maps loaded.");
		} else {
			log.info("Loaded " + maps.stream().mapToLong(GeoMap::getEntityCount).sum() + " entities on " + loadedMaps + " maps");
		}
	}

	private static Map<String, Node> loadMeshes() {
		Map<String, Node> geoms = new HashMap<>();
		try (FileChannel roChannel = FileChannel.open(GEO_DIR.resolve("models.mesh"))) {
			MappedByteBuffer geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, roChannel.size());
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

					int vertices = geo.getShort() & 0xFFFF;
					int verticesBytes = vertices * 3 * 4; // 3 floats per vertex (x, y, z), 4 bytes each
					m.setBuffer(VertexBuffer.Type.Position, 3, geo.slice(geo.position(), verticesBytes).asFloatBuffer());
					geo.position(geo.position() + verticesBytes);

					int faces = geo.getShort() & 0xFFFF;
					byte indexSize = geo.get();
					int facesBytes = faces * 3 * indexSize; // 3 vertex indices per face, `indexSize` bytes each
					switch (indexSize) {
						case 1 -> m.setBuffer(VertexBuffer.Type.Index, 3, geo.slice(geo.position(), facesBytes));
						case 2 -> m.setBuffer(VertexBuffer.Type.Index, 3, geo.slice(geo.position(), facesBytes).asShortBuffer());
						default -> throw new IOException("Index size " + indexSize + " is not supported");
					}
					geo.position(geo.position() + facesBytes);

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
			throw new GameServerError("Could not load meshes", e);
		}
		log.info("Loaded " + geoms.size() + " meshes");
		return geoms;
	}

	private static void loadWorld(GeoMap map, Map<String, Node> models, Set<String> missingMeshes) {
		Path geoFile = GEO_DIR.resolve(map.getMapId() + ".geo");
		if (!Files.isRegularFile(geoFile)) {
			WorldMapTemplate template = DataManager.WORLD_MAPS_DATA.getTemplate(map.getMapId());
			boolean shouldHaveEntities = template.getWorldSize() != 0 && !template.isPrison() && !template.getName().equalsIgnoreCase("IDTest_Dungeon") && !template.getName().equalsIgnoreCase("System_Basic");
			if (shouldHaveEntities)
				log.warn(geoFile + " is missing");
			return;
		}
		try (FileChannel roChannel = FileChannel.open((geoFile))) {
			MappedByteBuffer geo = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, roChannel.size());
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
						if (despawnableNode.type == DespawnableNode.DespawnableType.TOWN_OBJECT) {
							if (level > 8)
								throw new IllegalArgumentException(level + " doesn't fit in bit mask");
							despawnableNode.levelBitMask = level < 1 ? 0 : (byte) (1 << (level - 1));
						} else if (level != 0) {
							throw new IllegalArgumentException("Unexpected value in town level field for non-town entity");
						}
						node = despawnableNode;
					}
					Node nodeClone = attachToMapAndCreateZones(map, node, matrix3f, loc, scale);
					if (nodeClone instanceof DespawnableNode townEntity && townEntity.type == DespawnableNode.DespawnableType.TOWN_OBJECT) {
						// replicate client logic: find .cgfs for higher town levels or reuse current one
						for (int townLevel = level + 1; townLevel <= 5; townLevel++) {
							String townEntityName = name.replace("_01.cgf", "_0" + townLevel + ".cgf");
							Node model = models.get(townEntityName);
							if (model == null) {
								townEntity.levelBitMask |= (byte) (1 << (townLevel - 1));
							} else {
								DespawnableNode townNode = new DespawnableNode();
								townNode.copyFrom(model);
								townNode.type = townEntity.type;
								townNode.id = townEntity.id;
								townNode.levelBitMask = (byte) (1 << (townLevel - 1));
								townEntity = (DespawnableNode) attachToMapAndCreateZones(map, townNode, matrix3f, loc, scale);
							}
						}
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

	private static Node attachToMapAndCreateZones(GeoMap map, Node node, Matrix3f matrix3f, Vector3f loc, Vector3f scale) throws CloneNotSupportedException {
		Node nodeClone = (Node) attachChild(map, node, matrix3f, loc, scale);
		List<Spatial> children = nodeClone.getChildren();
		for (int c = 0; c < children.size(); c++) {
			createZone(children.get(c), map.getMapId(), children.size() == 1 ? 0 : c + 1);
		}
		return nodeClone;
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
