package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.materials.MaterialTemplate;

/**
 * @author Rolandas
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "materialTemplates" })
@XmlRootElement(name = "material_templates")
public class MaterialData {

	@XmlElement(name = "material")
	private List<MaterialTemplate> materialTemplates;

	@XmlTransient
	private final Map<Integer, MaterialTemplate> materialsById = new HashMap<>();

	@XmlTransient
	private final Set<Integer> skillIds = new HashSet<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (materialTemplates == null)
			return;

		for (MaterialTemplate template : materialTemplates) {
			materialsById.put(template.getId(), template);
			if (template.getSkills() != null)
				template.getSkills().forEach(skill -> skillIds.add(skill.getId()));
		}

		materialTemplates = null;
	}

	public MaterialTemplate getTemplate(int materialId) {
		return materialsById.get(materialId);
	}

	public boolean isMaterialSkill(int skillId) {
		return skillIds.contains(skillId);
	}

	public int size() {
		return materialsById.size();
	}

}
