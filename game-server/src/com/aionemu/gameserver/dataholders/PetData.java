package com.aionemu.gameserver.dataholders;

import com.aionemu.gameserver.model.templates.pet.PetTemplate;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
 */

@XmlRootElement(name = "pets")
@XmlAccessorType(XmlAccessType.FIELD)
public class PetData {
	@XmlElement(name = "pet")
	private List<PetTemplate> pets;
	@XmlTransient
	private final Map<Integer, PetTemplate> petData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (var pet : pets) {
			petData.put(pet.getTemplateId(), pet);
		}
		pets = null;
	}

	public int size() {
		return petData.size();
	}

	public PetTemplate getPetTemplate(int id) {
		return petData.get(id);
	}

	public Set<Integer> getPetIds() {
		return petData.keySet();
	}
}