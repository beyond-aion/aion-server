package com.aionemu.gameserver.geoEngine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.aionemu.gameserver.geoEngine.math.Matrix4f;
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
		maps.parallelStream().forEach(map -> {
            try {
                loadWorld(map, models, missingMeshes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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

	private static void loadWorld(GeoMap map, Map<String, Node> models, Set<String> missingMeshes) throws IOException {
		File geoFile = new File(GEO_DIR + map.getMapId() + ".geo");
		if (!geoFile.exists()) {
			if (DataManager.WORLD_MAPS_DATA.getTemplate(map.getMapId()).getWorldSize() != 0) // don't warn about inaccessible (test) maps
				log.warn(geoFile + " is missing");
			return;
		}
		/*
		List<Float> vertices = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		File bufferFile = new File("C:\\Users\\pooya\\OneDrive\\Desktop\\Test_objects\\"+DataManager.WORLD_MAPS_DATA.getTemplate(map.getMapId()).getCName()+".obj");
		FileWriter fw = new FileWriter(bufferFile);
		int trsize = 0;
		*/
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
				/*
				fw.write("o terrain\n");
				int rows = (int) Math.sqrt(size);
				for (int y = 0; y < rows-1; y++) {
					for (int x = 0; x < rows-1 ; x++) {
						float z1, z2, z3, z4;

						int z1Index = x + (y * rows);
						int z2Index = x + 1 + (y * rows);
						int z3Index = x + ((y + 1) * rows);
						int z4Index = x + 1 + ((y + 1) * rows);
						z1 = (terrainData[z1Index] / 32f);
						z2 = (terrainData[z2Index] / 32f);
						z3 = (terrainData[z3Index] / 32f);
						z4 = (terrainData[z4Index] / 32f);

						fw.write("v " + x * 2 + " " + z1 + " " + y * 2 + "\n"); // y, x
						fw.write("v " + (x + 1)* 2 + " " + z2 + " " + y * 2 + "\n"); // y, x + 1
						fw.write("v " + x * 2 + " " + z3 + " " + (y + 1) * 2 + "\n"); // y + 1, x

						fw.write("v " + x * 2 + " " + z3 + " " + (y + 1) * 2 + "\n"); // y + 1, x
						fw.write("v " + (x + 1)* 2 + " " + z2 + " " + y * 2 + "\n"); // y, x + 1
						fw.write("v " + (x + 1)*2 + " " + z4 + " " + (y + 1)*2 + "\n"); // y + 1 , x + 1
					}

				}
				for (int i = 0; i < (rows - 1) * (rows - 1) * 6 - 1; i += 3) {
					fw.write("f " + (i + 3) + " " + (i + 2) + " " + (i + 1) + "\n");
					trsize = i + 3;
				}
				*/
				map.setTerrainData(terrainData);
				if (containsMat) {
					map.setTerrainMaterials(terrainMaterials);
				}
			}
			// long cnt = 0;
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
					} /* else {
						List<Geometry> geoms = new ArrayList<>();
						getGeoms(geoms,node);
						Matrix4f matrix4f = new Matrix4f();
						matrix4f.loadIdentity();
						matrix4f.setRotationMatrix(matrix3f);
						matrix4f.scale(scale);
						matrix4f.setTranslation(loc);

						for (Geometry geom : geoms) {
							Mesh m = geom.getMesh();
							VertexBuffer vertexBuffer = m.getBuffer(VertexBuffer.Type.Position);
							FloatBuffer bufferData = (FloatBuffer)vertexBuffer.getData();
							bufferData.position(0);
							int n = bufferData.remaining();
							float[] bufferVert = new float[n]; // vertices.array();
							if (!bufferData.hasRemaining()) {
								log.debug(geom.getName() + " skipped.");
								continue;
							}
							bufferData.get(bufferVert);
							for (int i = 0; i < bufferVert.length; i+=3) {
								Vector3f vec = new Vector3f(bufferVert[i], bufferVert[i+1], bufferVert[i+2]);
								Vector3f finalVec = new Vector3f(
										vec.x * matrix4f.m00 + vec.y * matrix4f.m01 + vec.z * matrix4f.m02 + matrix4f.m03,
										vec.x * matrix4f.m10 + vec.y * matrix4f.m11 + vec.z * matrix4f.m12 + matrix4f.m13,
										vec.x * matrix4f.m20 + vec.y * matrix4f.m21 + vec.z * matrix4f.m22 + matrix4f.m23);

								vertices.add(finalVec.y);
								vertices.add(finalVec.z);
								vertices.add(finalVec.x);
							}
							bufferData.clear();
							VertexBuffer indexBuffer = m.getBuffer(VertexBuffer.Type.Index);
							ShortBuffer bufferData2 = (ShortBuffer)indexBuffer.getData();
							bufferData2.position(0);
							int n2 = bufferData2.remaining();
							short[] bufferVert2 = new short[n2]; // vertices.array();
							bufferData2.get(bufferVert2);
							for (int i = 0; i < bufferVert2.length; i+=3) {
								indices.add((bufferVert2[i] + 1));
								indices.add((bufferVert2[i + 1] + 1));
								indices.add((bufferVert2[i + 2] + 1));
							}
							bufferData2.clear();
							fw.write("o " + geom.getName() + "_" + cnt + "\n");
							cnt++;
							for (int i = 0; i < vertices.size(); i+=3) {
								 fw.write("v " + vertices.get(i) + " " + vertices.get(i+1) + " " + vertices.get(i+2) +"\n");
							}
							for (int i = 0; i < indices.size(); i+= 3) {
								 fw.write("f " + (trsize + indices.get(i)) + " " + (trsize + indices.get(i+1) ) + " " + (trsize + indices.get(i+2)) + "\n");

							}
							trsize += vertices.size()/3;
							vertices.clear();
							indices.clear();
						}
					}
					*/
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
		// fw.close();
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

	/*
	private static void getGeoms(List<Geometry> geoms, Spatial node) {
		if (node instanceof Node) {
			for (Spatial sp : ((Node) node).getChildren()) {
				getGeoms(geoms,sp);
			}
		} else if (node instanceof Geometry) {
			geoms.add((Geometry) node);
		}
	}
	*/
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
