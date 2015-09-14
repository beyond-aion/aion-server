package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.TShortObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

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
	private TShortObjectHashMap<PetDopingEntry> dopingsById = new TShortObjectHashMap<PetDopingEntry>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (PetDopingEntry dope : list)
			dopingsById.put(dope.getId(), dope);

		list.clear();
		list = null;
	}

	public int size() {
		return dopingsById.size();
	}

	public PetDopingEntry getDopingTemplate(short id) {
		return dopingsById.get(id);
	}

}
