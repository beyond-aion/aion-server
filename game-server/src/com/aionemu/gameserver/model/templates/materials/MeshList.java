package com.aionemu.gameserver.model.templates.materials;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MeshList", propOrder = { "meshMaterials" })
public class MeshList {

	@XmlElement(name = "mesh", required = true)
	protected List<MeshMaterial> meshMaterials;

	@XmlAttribute(name = "world_id", required = true)
	protected int worldId;

	@XmlTransient
	Map<String, Integer> materialIdsByPath = new HashMap<>();

	@XmlTransient
	Map<Integer, String> pathZones = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (meshMaterials == null)
			return;

		for (MeshMaterial meshMaterial : meshMaterials) {
			materialIdsByPath.put(meshMaterial.path, meshMaterial.materialId);
			pathZones.put(meshMaterial.path.hashCode(), meshMaterial.getZoneName());
			meshMaterial.path = null;
		}

		meshMaterials = null;
	}

	public int getWorldId() {
		return worldId;
	}

	/**
	 * Find material ID for the specific mesh
	 * 
	 * @param meshPath
	 *          Mesh geo path
	 * @return 0 if not found
	 */
	public int getMeshMaterialId(String meshPath) {
		Integer materialId = materialIdsByPath.get(meshPath);
		if (materialId == null)
			return 0;
		return materialId;
	}

	public Set<String> getMeshPaths() {
		return materialIdsByPath.keySet();
	}

	public String getZoneName(String meshPath) {
		return pathZones.get(meshPath.hashCode());
	}

	public int size() {
		return materialIdsByPath.size();
	}
}
