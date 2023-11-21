package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.pet.PetDopingEntry;

/**
 * @author Rolandas
 */
@XmlRootElement(name = "dopings")
@XmlAccessorType(XmlAccessType.FIELD)
public class PetDopingData {

	@XmlElement(name = "doping")
	private List<PetDopingEntry> list;

	@XmlTransient
	private final Map<Integer, PetDopingEntry> dopingsById = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (PetDopingEntry dope : list)
			dopingsById.put(dope.getId(), dope);
		list = null;
	}

	public int size() {
		return dopingsById.size();
	}

	public PetDopingEntry getDopingTemplate(int id) {
		return dopingsById.get(id);
	}

}
