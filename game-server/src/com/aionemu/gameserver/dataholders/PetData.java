package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.pet.PetTemplate;

/**
 * This is a container holding and serving all {@link PetTemplate} instances.<br>
 * 
 * @author IlBuono
 */
@XmlRootElement(name = "pets")
@XmlAccessorType(XmlAccessType.FIELD)
public class PetData {

	@XmlElement(name = "pet")
	private List<PetTemplate> pets;

	@XmlTransient
	private final Map<Integer, PetTemplate> petData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (PetTemplate pet : pets) {
			petData.put(pet.getTemplateId(), pet);
		}
		pets = null;
	}

	public int size() {
		return petData.size();
	}

	/**
	 * /** Returns an {@link PetTemplate} object with given id.
	 * 
	 * @param id
	 *          id of Pet
	 * @return PetTemplate object containing data about Pet with that id.
	 */
	public PetTemplate getPetTemplate(int id) {
		return petData.get(id);
	}

	public Set<Integer> getPetIds() {
		return petData.keySet();
	}
}
